package io.github.zezeg2.aisupport.core.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
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
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.validator.DefaultResultValidatorChain;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultAIFunction<T> {
    protected final String functionName;
    protected final String purpose;
    protected final List<Constraint> constraints;
    protected final Class<T> returnType;
    protected final OpenAiService service;
    protected final ObjectMapper mapper;
    protected final ConstructResolver resolver;
    protected final DefaultPromptManager promptManager;
    protected final DefaultResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    protected AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    protected void init(List<Argument<?>> args, String identifier) {
        if (promptManager.getContext().get(functionName) == null) {
            Prompt prompt = new Prompt(
                    functionName,
                    purpose,
                    resolveRefTypes(args),
                    createFunction(args),
                    createConstraints(constraints),
                    JsonUtils.convertMapToJson(BuildFormatUtil.getArgumentsFormatMap(args)),
                    buildResultFormat(),
                    BuildFormatUtil.getFormatString(FeedbackResponse.class)
            );
            promptManager.getContext().savePrompt(functionName, prompt);
            promptManager.addMessage(functionName, identifier, ROLE.SYSTEM, prompt.toString(), ContextType.PROMPT);
        }
        try {
            promptManager.addMessage(functionName, identifier, ROLE.USER, createValuesString(args), ContextType.PROMPT);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    protected String createValuesString(List<Argument<?>> args) throws JsonProcessingException {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueMap);
    }

    protected String resolveRefTypes(List<Argument<?>> args) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        classList.add(returnType);
        classList.add(FeedbackResponse.class);
        return resolver.resolve(classList);
    }

    protected String createConstraints(List<Constraint> constraintList) {
        return !constraintList.isEmpty() ? constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }


    public String createFunction(List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return TemplateConstants.FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, setReturnType());
    }

    protected T parseResponseWithValidate(HttpServletRequest request, ChatCompletionResult response) {
        String content = response.getChoices().get(0).getMessage().getContent();
        content = resultValidatorChain.validate(request, functionName, content);
        try {
            return mapper.readValue(content, returnType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildResultFormat() {
        return BuildFormatUtil.getFormatString(returnType);
    }

    public T execute(HttpServletRequest request, List<Argument<?>> args) {
        AIModel model = getDefaultModel();
        return execute(request, args, model);
    }

    public T execute(HttpServletRequest request, List<Argument<?>> args, AIModel model) {
        String identifier = promptManager.getIdentifier(request);
        init(args, identifier);
        ChatCompletionResult response = promptManager.exchangePromptMessages(functionName, identifier, model, true);
        return parseResponseWithValidate(request, response);
    }

    protected String setReturnType() {
        return returnType.getSimpleName();
    }
}
