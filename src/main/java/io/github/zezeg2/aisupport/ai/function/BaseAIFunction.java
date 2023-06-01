package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class BaseAIFunction<T> implements AIFunction<T> {
    protected final String functionName;
    protected final String purpose;
    protected final List<Constraint> constraints;
    protected final Class<T> returnType;
    protected final OpenAiService service;
    protected final ObjectMapper mapper;
    protected final ConstructResolver resolver;
    protected final BuildFormatUtil formatUtil;
    protected static final String FUNCTION_TEMPLATE = """
            @FunctionalInterface
            public interface FC {
                String %s(%s);
            }
                        
            public class Main {
                public static void main(String[] args) {
                    FC fc = (%s) -> {
                        return [RESULT] //TODO: [RESULT] is JsonString of `%s`
                    };
                }
            }
            """;
    protected final PromptManager promptManager;
    protected final ResultValidatorChain resultValidatorChain;
    protected final ExceptionValidator exceptionValidator;

    private final OpenAIProperties openAIProperties;

    @Override
    public T execute(List<Argument<?>> args) throws Exception {
        AIModel model = getDefaultModel();
        return execute(args, model);
    }

    @Override
    public T execute(List<Argument<?>> args, AIModel model) throws Exception {
        if (promptManager.getContext().getPrompt(functionName) == null) {
            String resultFormat = buildResultFormat();
            if (returnType.equals(STRUCTURE.LIST.getValue())) {
                resultFormat = """
                        [
                            %s,
                        ]
                        """.formatted(resultFormat);
            }
            if (returnType.equals(STRUCTURE.MAP.getValue())) {
                resultFormat = """
                        {
                            "key" : %s,
                        }
                        """.formatted(resultFormat);
            }
            Prompt prompt = new Prompt(
                    purpose,
                    resolveRefTypes(args),
                    createFunction(args),
                    createConstraints(constraints),
                    convertMapToJson(formatUtil.getArgumentsFormatMap(args)),
                    resultFormat,
                    formatUtil.getFormatString(FeedbackResponse.class));
            promptManager.initPromptContext(functionName, prompt);
        }
        promptManager.addMessage(functionName, ROLE.USER, ContextType.PROMPT, createValuesString(args));
        ChatCompletionResult response = promptManager.exchangeMessages(functionName, model, ContextType.PROMPT, true);
        return parseResponseWithValidate(response);
    }

    private AIModel getDefaultModel() {
        return ModelMapper.map(openAIProperties.getModel());
    }

    protected T parseResponse(ChatCompletionResult response) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }

    protected T parseResponseWithValidate(ChatCompletionResult response) throws Exception {
        String content = response.getChoices().get(0).getMessage().getContent();
        content = resultValidatorChain.validate(functionName, content);
        boolean success = false;
        T value = null;
        while (!success) {
            try {
                value = mapper.readValue(content, returnType);
                success = true;
            } catch (Exception exception) {
                content = exceptionValidator.validate(content, exception);
            }
        }

        return value;
    }


    protected boolean isBaseSupportType(Class<?> type) {
        return type.getSuperclass().equals(BaseSupportType.class);
    }

    protected String convertMapToJson(Map<String, Object> inputDescMap) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputDescMap);
        } catch (Exception e) {
            throw new CustomJsonException("Failed to convert map to JSON", e);
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
}
