package com.jbyee.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbyee.common.enums.ROLE;
import com.jbyee.resolver.ConstructResolver;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;


import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
public class AISupporter {

    private final OpenAiService service;

    private ConstructResolver resolver;


    public Map<String, Object> aiFunction(String functionName, Optional<List<Argument>> args, Optional<List<Constraint>> constraintList, String description, String model) throws JsonProcessingException {
        List<Argument> unwrappedArgs = args.orElse(List.of());
        String function = createFunction(null, functionName, unwrappedArgs);
        String refTypes = resolveRefTypes(unwrappedArgs, null);
        String constraints = constraintList.orElse(List.of()).stream().map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description()).collect(Collectors.joining("\n- ", "- ", "\n"));
        List<ChatMessage> messages = createChatMessages(description, refTypes, functionName, function, unwrappedArgs, constraints);
        ChatCompletionResult response = executeChatCompletion(model, messages);
        return parseResponse(response, LinkedHashMap.class);
    }

    public <T> T aiFunction(String functionName, Class<T> returnType, Optional<List<Argument>> args,Optional< List<Constraint>> constraintList, String description, String model) throws JsonProcessingException {
        List<Argument> unwrappedArgs = args.orElse(List.of());
        String function = createFunction(returnType, functionName, unwrappedArgs);
        String refTypes = resolveRefTypes(unwrappedArgs, returnType);
        String constraints = constraintList.orElse(List.of()).stream().map(constraint -> !constraint.topic().isBlank() ? constraint.topic() + ": " + constraint.description() : constraint.description()).collect(Collectors.joining("\n- ", "- ", "\n"));
        List<ChatMessage> messages = createChatMessages(description, refTypes, functionName, function, unwrappedArgs, constraints);
        ChatCompletionResult response = executeChatCompletion(model, messages);
        return parseResponse(response, returnType);
    }

    private <T> String createFunction(Class<T> returnType, String functionName,List<Argument> args) {
        String fieldsString = args.stream().map(Argument::getField).collect(Collectors.joining(", "));
        String fieldTypesString = args.stream()
                .map(argument -> argument.getType() + " " + argument.getField())
                .collect(Collectors.joining(", "));

        return """
                @FunctionalInterface
                public interface FC {
                    %s %s(%s);
                }
                public class Main {
                    public static void main(String[] args) {
                        FC fc = (%s) -> {
                            //TODO: implement or just return result
                            return //TODO: fill your result here. your return will be converted by ObjectMapper.
                        };
                    }
                }
                """.formatted(returnType != null ? returnType.getSimpleName() : "Map<String, Object>", functionName, fieldTypesString, fieldsString);
    }

    private String resolveRefTypes(List<Argument> args, Class<?> returnType) {
        Set<Class<?>> classList = args.stream().map(Argument::type).collect(Collectors.toSet());
        if (returnType != null) classList.add(returnType);
        return resolver.resolve(classList);
    }

    private List<ChatMessage> createChatMessages(String description, String refTypes, String functionName, String function, List<Argument> args, String constraints) {
        String valuesString = args.stream().map(Argument::getValue).collect(Collectors.joining(", "));

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
        ObjectMapper mapper = new ObjectMapper();
        return !returnType.getSimpleName().equals("LinkedHashMap") ? mapper.convertValue(content, returnType) : mapper.readValue(content, returnType);
    }
}
