package com.jbyee.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbyee.common.Role;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AISupporter {

    private final OpenAiService service;


    public <T> T aiFunction(String function, Class<T> returnType, Object[] args, String description, String model) {

        // parse args to comma separated string
        String argsString = String.join(", ", Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", ")));
        List<ChatMessage> messages = List.of(
                new ChatMessage(Role.SYSTEM.getValue(), "You are now the following TypeScript function: ```# " + description + "\n" + function + "```\n\nOnly respond with your `return` value. Do not include any other explanatory text in your response."),
                new ChatMessage(Role.USER.getValue(), argsString)
        );

        ChatCompletionResult response = service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build());

//        Arrays.stream(returnType.getFields()).map(field -> field.getType())

        // Use ObjectMapper to convert the response to the specified return type
        String content = response.getChoices().get(0).getMessage().getContent();
        ObjectMapper objectMapper = new ObjectMapper();


        T result = objectMapper.convertValue(content, returnType);
        return result;
    }
}
