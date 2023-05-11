package com.jbyee.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbyee.common.enums.ROLE;
import com.jbyee.resolver.ConstructResolver;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;


import java.util.List;

@RequiredArgsConstructor
public class AISupporter {

    private final OpenAiService service;

    private final ConstructResolver resolver;


//    public <T> T aiFunction(){
//
//    }
    public <T> T aiFunction(String function, Class<T> returnType, Object[] args, String description, String model) {
        // parse args to comma separated string
        String argsString = String.join(", ", args.toString());
        List<ChatMessage> messages = List.of(
                new ChatMessage(ROLE.SYSTEM.getValue(), "You are now the following TypeScript function: ```# " + description + "\n" + function + "```\n\nOnly respond with your `return` value. Do not include any other explanatory text in your response."),
                new ChatMessage(ROLE.USER.getValue(), argsString)
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
