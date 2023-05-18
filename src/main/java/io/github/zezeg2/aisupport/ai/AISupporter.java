package io.github.zezeg2.aisupport.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.*;
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

    public Map<String, Object> aiFunction(String functionName, List<Argument<?>> args, List<Constraint> constraintList, String description, GPTModel model) throws JsonProcessingException {
        return aiFunction(functionName, DEFAULT_RETURN_TYPE, args, constraintList, description, model);
    }

    @Deprecated
    public <T> T aiFunction(String functionName, Class<T> returnType, List<Argument<?>> args, List<Constraint> constraintList, String description, GPTModel model) throws JsonProcessingException {
        String functionTemplate = createFunctionTemplate(returnType, functionName, args);
        String refTypes = resolveRefTypes(args, returnType);
        String constraints = createConstraints(constraintList);
        List<ChatMessage> messages = createChatMessages(description, refTypes, functionName, functionTemplate, args, constraints);
        ChatCompletionResult response = executeChatCompletion(model.getValue(), messages);
        return parseResponse(response, returnType);
    }

    public <T> AIFunctionDeprecated<T> createFunction(String functionName, String description, WRAPPING wrapping, Class<T> returnType, List<Constraint> constraintList) {
        return new AIFunctionDeprecated<T>(functionName, description, constraintList, wrapping, returnType, service, mapper, resolver);
    }

    public <T> AIFunction<T> createSingleFunction(String functionName, String description, Class<T> returnType, List<Constraint> constraintList) {
        return new AISingleFunction<>(functionName, description, constraintList, returnType, service, mapper, resolver);
    }

    public <T> AIFunction<List<T>> createListFunction(String functionName, String description, Class<T> returnType, List<Constraint> constraintList) {
        return new AIListFunction<>(functionName, description, constraintList, (Class<List<T>>)(Class<?>)List.class, service, mapper, resolver, returnType);
    }

    public <T> AIFunction<Map<String, T>> createMapFunction(String functionName, String description, Class<T> returnType, List<Constraint> constraintList) {
        return new AIMapFunction<>(functionName, description, constraintList, (Class<Map<String ,T>>)(Class<?>)Map.class, service, mapper, resolver, returnType);
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

    private static ChatMessage createTemplate(String description, String refTypes, String function, String constraints) {
        return new ChatMessage(ROLE.SYSTEM.getValue(), "You are now the following Java Lambda function: \n"
                + "```java \n"
                + refTypes + "\n"
                + "// description: This function" + description + "\n"
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
