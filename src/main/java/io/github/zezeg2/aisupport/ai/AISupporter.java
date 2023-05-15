package io.github.zezeg2.aisupport.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.function.Argument;
import io.github.zezeg2.aisupport.ai.function.Constraint;
import io.github.zezeg2.aisupport.ai.model.gpt.GPTModel;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
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

    public Map<String, Object> aiFunction(String functionName, List<Argument> args, List<Constraint> constraintList, String description, GPTModel model) throws JsonProcessingException {
        return aiFunction(functionName, DEFAULT_RETURN_TYPE, args, constraintList, description, model);
    }

    public <T> T aiFunction(String functionName, Class<T> returnType, List<Argument> args, List<Constraint> constraintList, String description, GPTModel model) throws JsonProcessingException {
        String function = createFunction(returnType, functionName, args);
        String refTypes = resolveRefTypes(args, returnType);
        String constraints = createConstraints(constraintList);
        List<ChatMessage> messages = createChatMessages(description, refTypes, functionName, function, args, constraints);
        ChatCompletionResult response = executeChatCompletion(model.getValue(), messages);
        return parseResponse(response, returnType);
    }

    private <T> String createFunction(Class<T> returnType, String functionName, List<Argument> args) {
        String fieldsString = args.stream().map(Argument::getField).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getType() + " " + argument.getField())
                .collect(Collectors.joining(", "));

        return FUNCTION_TEMPLATE.formatted(functionName, fieldTypesString, fieldsString, returnType.getSimpleName());
    }

    private String resolveRefTypes(List<Argument> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::type).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }

    private String createConstraints(List<Constraint> constraintList) {
        return constraintList.stream()
                .map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description())
                .collect(Collectors.joining("\n- ", "\n- ", "\n"));
    }

    private List<ChatMessage> createChatMessages(String description, String refTypes, String functionName, String function, List<Argument> args, String constraints) {
        String valuesString = args.stream().map(argument -> {
            String value = argument.getType().equals("String") ? "\"" + argument.getValue() + "\"" : argument.getValue();
            return argument.getField() + ": " + value;
        }).collect(Collectors.joining("\n"));

        return List.of(
                new ChatMessage(ROLE.SYSTEM.getValue(), "You are now the following Java Lambda function: \n"
                        + "```java \n"
                        + "// description: " + description + "\n"
                        + refTypes + "\n"
                        + functionName + "\n"
                        + function + "\n"
                        + "```\n"
                        + "- Only respond with your `return` value. Do not include any other explanatory text in your response."
                        + constraints
                ),
                new ChatMessage(ROLE.USER.getValue(), valuesString)
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
        return mapper.readValue(content, returnType);
    }
}
