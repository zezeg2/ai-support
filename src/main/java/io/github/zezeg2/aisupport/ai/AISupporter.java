package io.github.zezeg2.aisupport.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.AIFunction;
import io.github.zezeg2.aisupport.ai.function.AIListFunction;
import io.github.zezeg2.aisupport.ai.function.AIMapFunction;
import io.github.zezeg2.aisupport.ai.function.AISingleFunction;
import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.model.gpt.GPTModel;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.WRAPPING;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AISupporter {
    public AISupporter(String apiKey) {
        this.service = new OpenAiService(apiKey);
        this.mapper = new ObjectMapper();
        this.resolver = new JAVAConstructResolver();
    }

    public AISupporter(String apiKey, int timeout) {
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(timeout));
        this.mapper = new ObjectMapper();
        this.resolver = new JAVAConstructResolver();
    }

    public AISupporter(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver) {
        this.service = service;
        this.mapper = mapper;
        this.resolver = resolver;
    }

    private static final String FUNCTION_TEMPLATE = """
            @FunctionalInterface
            public interface FC {
                String %s(%s);
            }
            public class Main {
                public static void main(String[] args) {
                    FC fc = (%s) -> {
                        return [RESULT] //TODO: [RESULT] is JsonString of `%s.class`
                    };
                }
            }
            """;

    private static final Class<LinkedHashMap> DEFAULT_RETURN_TYPE = LinkedHashMap.class;
    private final OpenAiService service;
    private final ObjectMapper mapper;

    private final ConstructResolver resolver;

    @Deprecated
    public Map<String, Object> aiFunction(String functionName, List<Argument<?>> args, List<Constraint> constraintList, String purpose, GPTModel model) throws JsonProcessingException {
        return aiFunction(functionName, DEFAULT_RETURN_TYPE, args, constraintList, purpose, model);
    }

    @Deprecated
    public <T> T aiFunction(String functionName, Class<T> returnType, List<Argument<?>> args, List<Constraint> constraintList, String purpose, GPTModel model) throws JsonProcessingException {
        String functionTemplate = createFunctionTemplate(returnType, functionName, args);
        String refTypes = resolveRefTypes(args, returnType);
        String constraints = createConstraints(constraintList);
        List<ChatMessage> messages = createChatMessages(purpose, refTypes, functionName, functionTemplate, args, constraints);
        ChatCompletionResult response = executeChatCompletion(model.getValue(), messages);
        return parseResponse(response, returnType);
    }

    public <T> AIFunction<?> createFunction(WRAPPING wrapping, String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return switch (wrapping) {
            case NONE ->
                    new AISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver);
            case LIST ->
                    new AIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, returnType);
            case MAP ->
                    new AIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, returnType);
        };
    }

    public <T> AIFunction<T> createSingleFunction(String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return new AISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver);
    }

    public <T> AIFunction<List<T>> createListFunction(String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return new AIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, returnType);
    }

    public <T> AIFunction<Map<String, T>> createMapFunction(String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return new AIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, returnType);
    }

    private <T> String createFunctionTemplate(Class<T> returnType, String functionName, List<Argument<?>> args) {
        String fieldsString = args.stream().map(Argument::getFieldName).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getTypeName() + " " + argument.getFieldName())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }

    private String resolveRefTypes(List<Argument<?>> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::getType).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }


    private String createConstraints(List<Constraint> constraintList) {
        return constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "\n- ", "\n"));
    }

    private List<ChatMessage> createChatMessages(String description, String refTypes, String functionName, String function, List<Argument<?>> args, String constraints) {
        String valuesString = args.stream().map(argument -> {
            String value = argument.getTypeName().equals("String") ? "\"" + argument.getValueToString() + "\"" : argument.getValueToString();
            return argument.getFieldName() + ": " + value;
        }).collect(Collectors.joining("\n"));

        return List.of(
                createTemplate(description, refTypes, function, constraints),
                new ChatMessage(ROLE.USER.getValue(), valuesString)
        );
    }

    private static ChatMessage createTemplate(String purpose, String refTypes, String function, String constraints) {
        return new ChatMessage(ROLE.SYSTEM.getValue(), "You are now the following Java Lambda function: \n"
                + "```java \n"
                + refTypes + "\n"
                + "// Purpose: " + purpose + "\n"
                + function + "\n"
                + "```\n"
                + "- Only respond with your `return` value. Do not include any other explanatory text in your response."
                + constraints
        );
    }

    private ChatCompletionResult executeChatCompletion(String model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build());
    }

    private <T> T parseResponse(ChatCompletionResult response, Class<T> returnType) throws JsonProcessingException {
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return mapper.readValue(content, returnType);
    }
}
