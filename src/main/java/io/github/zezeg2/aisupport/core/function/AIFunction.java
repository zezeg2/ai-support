package io.github.zezeg2.aisupport.core.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.core.validator.ResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AIFunction<T> {
    private final String functionName;
    private final String command;
    private final List<Constraint> constraints;
    private final Class<T> returnType;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final PromptManager promptManager;
    private final ResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;
    private final double topP;

    /**
     * Retrieves the default AI model.
     *
     * @return The default AI model.
     */
    private AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    /**
     * Initializes the AIFunction with the specified execution parameters.
     *
     * @param params The execution parameters.
     */
    private void init(ExecuteParameters<T> params) {
        List<Argument<?>> args = params.getArgs();
        String identifier = params.getIdentifier();
        T example = params.getExample();

        Prompt prompt = promptManager.getContext().get(functionName);
        if (prompt == null) {
            prompt = new Prompt(
                    functionName,
                    command,
                    createConstraints(constraints),
                    JsonUtils.convertMapToJson(BuildFormatUtil.getArgumentsFormatMap(args)),
                    BuildFormatUtil.getFormatString(returnType),
                    BuildFormatUtil.getFormatString(FeedbackResponse.class),
                    topP
            );
            promptManager.getContext().savePrompt(functionName, prompt);
        }
        if (promptManager.getContext().getPromptChatMessages(functionName, identifier).getContent().isEmpty()) {
            if (example == null)
                promptManager.addMessage(functionName, identifier, ROLE.SYSTEM, prompt.generate(), ContextType.PROMPT);
            else
                promptManager.addMessage(functionName, identifier, ROLE.SYSTEM, prompt.generate(mapper, example), ContextType.PROMPT);
        }
        if (example != null) {
            ChatMessage systemMessage = promptManager.getContext().getPromptChatMessages(functionName, identifier).getContent().stream().filter(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue())).findFirst().orElseThrow();
            String generatedSystemMessageContent = prompt.generate(mapper, example);
            if (!systemMessage.getContent().equals(generatedSystemMessageContent)) {
                systemMessage.setContent(generatedSystemMessageContent);
                promptManager.getContext().savePromptMessages(functionName, identifier, systemMessage);
            }
        }
        promptManager.addMessage(functionName, identifier, ROLE.USER, createValuesString(args), ContextType.PROMPT);
    }

    /**
     * Creates a string representation of the argument values.
     *
     * @param args The list of arguments.
     * @return The string representation of the argument values.
     */
    private String createValuesString(List<Argument<?>> args) {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves the reference types used in the AIFunction.
     *
     * @param args The list of arguments.
     * @return The resolved reference types.
     */
    private String resolveRefTypes(List<Argument<?>> args) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        classList.add(returnType);
        classList.add(FeedbackResponse.class);
        return resolver.resolve(classList);
    }

    /**
     * Creates the constraints
     * /**
     * Creates the constraints string.
     *
     * @param constraintList The list of constraints.
     * @return The constraints string.
     */
    private String createConstraints(List<Constraint> constraintList) {
        return !constraintList.isEmpty() ? constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }

    /**
     * Creates the function string based on the argument list.
     *
     * @param args The list of arguments.
     * @return The function string.
     */
    public String createFunction(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return TemplateConstants.FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }

    /**
     * Parses the response from the AI model and validates it using the result validator chain.
     *
     * @param params   The execution parameters.
     * @param response The chat completion result.
     * @return The parsed and validated response object.
     */
    private T parseResponseWithValidate(ExecuteParameters<T> params, ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        String valuesString = createValuesString(params.getArgs());
        content = resultValidatorChain.validate(functionName, params.getIdentifier(), valuesString, content);
        try {
            return mapper.readValue(content, returnType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Executes the AIFunction with the specified execution parameters.
     *
     * @param params The execution parameters.
     * @return The result of the execution.
     */
    public T execute(ExecuteParameters<T> params) {
        if (params.getModel() == null) params.setModel(getDefaultModel());
        if (params.getIdentifier() == null) params.setIdentifier("temp-identifier-" + UUID.randomUUID());
        init(params);
        ChatCompletionResult response = promptManager.exchangePromptMessages(functionName, params.getIdentifier(), params.getModel(), topP, true);
        return parseResponseWithValidate(params, response);
    }
}
