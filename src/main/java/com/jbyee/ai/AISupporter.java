package com.jbyee.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbyee.common.enums.ROLE;
import com.jbyee.resolver.ConstructResolver;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
public class AISupporter {

    private final OpenAiService service;

    private ConstructResolver resolver;


    public <T> T aiFunction(String functionName, Class<T> returnType, List<ArgumentRecord> args, String description, String model) {
        // parse args to comma separated string

        String filedsString = args.stream().map(ArgumentRecord::getFiled).collect(Collectors.joining(", "));
        String filedTypesString = args.stream().map(ArgumentRecord::getTypeField).collect(Collectors.joining(", "));
        String valuesString = args.stream().map(ArgumentRecord::getValue).collect(Collectors.joining(", "));
        String function = """
                @FunctionalInterface
                public interface FC {
                    %s %s(%s);
                }
                public class Main {
                    public static void main(String[] args) {
                        FC fc = (%s) -> {
                            
                        };
                    }
                }
                
                """.formatted(functionName, returnType.getSimpleName(), filedTypesString, filedsString);
        List<? extends Class<?>> classList = args.stream().map(ArgumentRecord::type).toList();
        String refTypes = resolver.resolve((List<Class<?>>) classList);
        List<ChatMessage> messages = List.of(
                new ChatMessage(ROLE.SYSTEM.getValue(), "You are now the following Java Lambda function: "
                        + "```# "
                        + description + "\n"
                        + refTypes + "\n"
                        + functionName + "\n"
                        + function + "\n"
                        + "```\n" +
                        "Only respond with your `return` value. Do not include any other explanatory text in your response."),
                new ChatMessage(ROLE.USER.getValue(), valuesString)
        );

        ChatCompletionResult response = service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build());

        // Use ObjectMapper to convert the response to the specified return type
        String content = response.getChoices().get(0).getMessage().getContent();
        ObjectMapper objectMapper = new ObjectMapper();
        T result = objectMapper.convertValue(content, returnType);
        return result;
    }
}
