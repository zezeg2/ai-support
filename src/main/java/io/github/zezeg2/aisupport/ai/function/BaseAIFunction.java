package io.github.zezeg2.aisupport.ai.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.argument.MapArgument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.context.PromptContext;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.validate.Validator;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.context.ContextIdentifierProvider;
import io.github.zezeg2.aisupport.common.Supportable;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import io.github.zezeg2.aisupport.common.exceptions.CustomJsonException;
import io.github.zezeg2.aisupport.common.exceptions.NotInitiatedContextException;
import io.github.zezeg2.aisupport.common.exceptions.NotSupportArgumentException;
import io.github.zezeg2.aisupport.context.ThreadNameIdentifierProvider;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public abstract class BaseAIFunction<T> implements AIFunction<T> {
    protected final String functionName;
    protected final String purpose;
    protected final List<Constraint> constraints;
    protected final Class<T> returnType;
    protected final OpenAiService service;
    protected final ObjectMapper mapper;
    protected final ConstructResolver resolver;
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
    protected static final String PROMPT_TEMPLATE = """
            You are now the following Java Lambda function
            ```java
            %s
                        
            // Purpose: %s
            %s
            ```
            Constraints
            - Only respond with your `return` value. Do not include any other explanatory text in your response.
            %s
            - Input Format : %s
            - Result Format : %s
            """;
    @Getter
    @Setter
    protected List<Validator> validators;
    @Getter
    @Setter
    protected ContextIdentifierProvider idProvider;

    public BaseAIFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver) {
        this.functionName = functionName;
        this.purpose = purpose;
        this.constraints = constraints;
        this.returnType = returnType;
        this.service = service;
        this.mapper = mapper;
        this.resolver = resolver;
        this.validators = null;
        this.idProvider = new ThreadNameIdentifierProvider();
    }

    public BaseAIFunction(String functionName, String purpose, List<Constraint> constraints, Class<T> returnType, OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, List<Validator> validators, ContextIdentifierProvider idProvider) {
        this.functionName = functionName;
        this.purpose = purpose;
        this.constraints = constraints;
        this.returnType = returnType;
        this.service = service;
        this.mapper = mapper;
        this.resolver = resolver;
        this.validators = validators;
        this.idProvider = idProvider;
    }

    @Override
    public T execute(List<Argument<?>> args, AIModel model) throws Exception {
        List<ChatMessage> messages = createMessages(args);
        ChatCompletionResult response = createChatCompletion(model, messages);
        return parseResponse(response);
    }

    @Override
    public T executeWithContext(List<Argument<?>> args, AIModel model) throws Exception {
        ChatMessage responseMessage = exchangeMessages(args, model);
        return parseResponseWithValidate(responseMessage);
    }

    protected void addMessage(ROLE role, String message) {
        String identifier = idProvider.getId();
        Map<String, List<ChatMessage>> promptMessageContext = PromptContext.getPrompt(functionName).getPromptMessageContext();
        if (!promptMessageContext.containsKey(identifier))
            promptMessageContext.put(identifier, new CopyOnWriteArrayList<>());

        List<ChatMessage> chatMessages = promptMessageContext.get(identifier);
        if (!chatMessages.isEmpty()) chatMessages.add(new ChatMessage(role.getValue(), message));
        else {
            if (role.equals(ROLE.SYSTEM)) chatMessages.add(new ChatMessage(role.getValue(), message));
            else throw new NotInitiatedContextException();
        }
    }

    protected void initContext(List<Argument<?>> args) throws Exception {
        if (!PromptContext.containsPrompt(functionName)) {
            PromptContext.addPromptToContext(functionName, new Prompt(
                    purpose,
                    resolveRefTypes(args, returnType),
                    createFunction(args),
                    createConstraints(constraints),
                    buildInputFormat(args),
                    buildResultFormat())
            );
        }
        Prompt prompt = PromptContext.getPrompt(functionName);
        Map<String, List<ChatMessage>> promptMessageContext = prompt.getPromptMessageContext();
        if (!promptMessageContext.containsKey(idProvider.getId())) {
            addMessage(ROLE.SYSTEM, prompt.toString());
        }
    }

    protected ChatMessage exchangeMessages(List<Argument<?>> args, AIModel model) throws Exception {
        initContext(args);
        addMessage(ROLE.USER, createValuesString(args));
        List<ChatMessage> contextMessages = PromptContext.getPromptMessageContext(functionName).get(idProvider.getId());
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        contextMessages.add(responseMessage);
        return responseMessage;
    }

    protected T parseResponse(ChatCompletionResult response) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }

    protected T parseResponseWithValidate(ChatMessage message) throws JsonProcessingException {
        String content = message.getContent();
        boolean success = false;
        System.out.println(content);
        T value = null;
//        while (!success) {
//            try {
//                value = mapper.readValue(content, returnType);
//                success = true;
//            } catch (JsonProcessingException e) {
//                value = null;
//                // TODO: 2023/05/23 Validator 에서 content 스트링을 검증하는 로직 추가(디폴트: JsonProcess Validate, 셀프 피드백 : Feedback Validator)
//            }
//        }
        value = mapper.readValue(content, returnType);
        return value;
    }

    protected String buildInputFormat(List<Argument<?>> args) throws Exception {
        Map<String, Object> inputDescMap = new LinkedHashMap<>();
        for (Argument<?> argument : args) {
            addToInputDescMap(inputDescMap, argument);
        }

        return convertMapToJson(inputDescMap);
    }

    protected void addToInputDescMap(Map<String, Object> inputDescMap, Argument<?> argument) throws Exception {
        Class<?> argWrapping = argument.getWrapping();

        Map<String, Object> descMap = generateDescMap(argument, argument.getType());
        if (argWrapping == null) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), entry.getValue());
            }
        } else if (argWrapping.equals(WRAPPING.LIST.getValue())) {
            if (!descMap.isEmpty()) {
                Map.Entry<String, Object> entry = descMap.entrySet().iterator().next();
                inputDescMap.put(argument.getFieldName(), List.of(entry.getValue()));
            }

        } else if (argWrapping.equals(WRAPPING.MAP.getValue())) {
            Map<String, Map<String, Object>> transformedMap = new LinkedHashMap<>();
            for (String key : ((MapArgument<?>) argument).getValue().keySet()) {
                transformedMap.put(key, descMap);
            }
            inputDescMap.put(argument.getFieldName(), transformedMap);
        } else {
            throw new NotSupportArgumentException();
        }
    }

    protected Map<String, Object> generateDescMap(Argument<?> argument, Class<?> type) throws Exception {
        if (isBaseSupportType(type)) {
            Supportable supportable = (Supportable) type.getConstructor().newInstance();
            return Map.of(argument.getFieldName(), supportable.getFormatMap());
        } else if (argument.getDesc() == null) {
            return Map.of(argument.getFieldName(), argument.getFieldName());
        } else {
            return Map.of(argument.getFieldName(), argument.getDesc());
        }
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

    protected List<ChatMessage> createMessages(List<Argument<?>> args) throws Exception {
        String prompt = createPrompt(
                resolveRefTypes(args, returnType),
                purpose,
                createFunction(args),
                createConstraints(constraints),
                buildInputFormat(args),
                buildResultFormat()
        );
        String valuesString = createValuesString(args);
        return List.of(new ChatMessage(ROLE.SYSTEM.getValue(), prompt), new ChatMessage(ROLE.USER.getValue(), valuesString));
    }

    protected String createValuesString(List<Argument<?>> args) throws JsonProcessingException {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        args.forEach(argument -> valueMap.put(argument.getFieldName(), argument.getValue()));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueMap);
    }

    protected ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build());
    }

    protected String resolveRefTypes(List<Argument<?>> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }

    protected String createConstraints(List<Constraint> constraintList) {
        return !constraintList.isEmpty() ? constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "- ", "\n")) : "";
    }
}
