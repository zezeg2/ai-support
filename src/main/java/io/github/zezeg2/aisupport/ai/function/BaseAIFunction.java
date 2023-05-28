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
import io.github.zezeg2.aisupport.ai.validator.DefaultResultValidator;
import io.github.zezeg2.aisupport.ai.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;
import io.github.zezeg2.aisupport.context.LocalPromptContextHolder;
import io.github.zezeg2.aisupport.context.ThreadNameIdentifierProvider;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Getter
    protected final PromptManager promptManager;
    protected final ResultValidatorChain resultValidatorChain;

    public BaseAIFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, BuildFormatUtil formatUtil) {
        this.functionName = functionName;
        this.purpose = purpose;
        this.constraints = constraints;
        this.returnType = returnType;
        this.service = service;
        this.mapper = mapper;
        this.resolver = resolver;
        this.formatUtil = formatUtil;
        this.promptManager = new PromptManager(service, new LocalPromptContextHolder(), new ThreadNameIdentifierProvider());
        this.resultValidatorChain = new ResultValidatorChain(List.of(new DefaultResultValidator(promptManager, formatUtil)));
    }

    public BaseAIFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, BuildFormatUtil formatUtil, PromptManager promptManager, ResultValidatorChain resultValidatorChain) {
        this.functionName = functionName;
        this.purpose = purpose;
        this.constraints = constraints;
        this.returnType = returnType;
        this.service = service;
        this.mapper = mapper;
        this.resolver = resolver;
        this.formatUtil = formatUtil;
        this.promptManager = promptManager;
        this.resultValidatorChain = resultValidatorChain;
    }

    @Override
    public T execute(List<Argument<?>> args, AIModel model) throws Exception {
        if (promptManager.getContext().getPrompt(functionName) == null) {
            String resultFormat = buildResultFormat();
            if (returnType.equals(STRUCTURE.LIST.getValue())){
                resultFormat = """
                [
                    %s,
                ]
                """.formatted(resultFormat);
            }
            if (returnType.equals(STRUCTURE.MAP.getValue())){
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

    protected T parseResponse(ChatCompletionResult response) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }

    protected T parseResponseWithValidate(ChatCompletionResult response) throws Exception {
        String content = response.getChoices().get(0).getMessage().getContent();
        content = resultValidatorChain.validate(functionName, content);
        return mapper.readValue(content, returnType);
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
