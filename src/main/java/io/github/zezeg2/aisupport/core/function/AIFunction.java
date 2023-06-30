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
    private final String purpose;
    private final List<Constraint> constraints;
    private final Class<T> returnType;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final PromptManager promptManager;
    private final ResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;
    private final double topP;

    private AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    private void init(ExecuteParameters<T> params) {
        List<Argument<?>> args = params.getArgs();
        String identifier = params.getIdentifier();
        T example = params.getExample();

        Prompt prompt = promptManager.getContext().get(functionName);
        if (prompt == null) {
            prompt = new Prompt(
                    functionName,
                    purpose,
                    resolveRefTypes(args),
                    createFunction(args),
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
        try {
            promptManager.addMessage(functionName, identifier, ROLE.USER, createValuesString(args), ContextType.PROMPT);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private String createValuesString(List<Argument<?>> args) throws JsonProcessingException {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueMap);
    }

    private String resolveRefTypes(List<Argument<?>> args) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        classList.add(returnType);
        classList.add(FeedbackResponse.class);
        return resolver.resolve(classList);
    }

    private String createConstraints(List<Constraint> constraintList) {
        return !constraintList.isEmpty() ? constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }


    public String createFunction(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return TemplateConstants.FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }

    private T parseResponseWithValidate(String identifier, ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        content = resultValidatorChain.validate(identifier, functionName, content);
        try {
            return mapper.readValue(content, returnType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public T execute(ExecuteParameters<T> params) {
        if (params.getModel() == null) params.setModel(getDefaultModel());
        if (params.getIdentifier() == null) params.setIdentifier("temp-identifier-" + UUID.randomUUID());
        init(params);
        ChatCompletionResult response = promptManager.exchangePromptMessages(functionName, params.getIdentifier(), params.getModel(), topP, true);
        return parseResponseWithValidate(params.getIdentifier(), response);
    }
}
