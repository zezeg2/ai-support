package io.github.zezeg2.aisupport.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class RedisPromptContextHolder implements PromptContextHolder {

    private final HashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public RedisPromptContextHolder(RedisTemplate<String, String> template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public boolean contains(String namespace) {
        return hashOperations.hasKey(namespace, "prompt");
    }

    @Override
    public void savePrompt(String namespace, Prompt prompt) {
        try {
            String serializedPrompt = mapper.writeValueAsString(prompt);
            hashOperations.put(namespace, "prompt", serializedPrompt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the prompt", e);
        }
    }

    @Override
    public Prompt get(String namespace) {
        String serializedPrompt = hashOperations.get(namespace, "prompt");
        try {
            return mapper.readValue(serializedPrompt, Prompt.class);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing the prompt", e);
        }
    }

    @Override
    public PromptMessages getPromptChatMessages(String namespace, String identifier) {
        String serializedPromptMessages = hashOperations.get(namespace, identifier);
        if (serializedPromptMessages == null) {
            PromptMessages promptMessages = PromptMessages.builder().identifier(identifier).content(new CopyOnWriteArrayList<>()).build();
            try {
                hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing the prompt messages", e);
            }
            return promptMessages;
        } else {
            try {
                return mapper.readValue(serializedPromptMessages, PromptMessages.class);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing the prompt messages", e);
            }
        }
    }

    @Override
    public FeedbackMessages getFeedbackChatMessages(String namespace, String identifier) {
        String serializedFeedbackMessages = hashOperations.get(namespace, identifier);
        if (serializedFeedbackMessages == null) {
            FeedbackMessages feedbackMessages = FeedbackMessages.builder().identifier(identifier).content(new CopyOnWriteArrayList<>()).build();
            try {
                hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing the feedback messages", e);
            }
            return feedbackMessages;
        } else {
            try {
                return mapper.readValue(serializedFeedbackMessages, FeedbackMessages.class);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing the feedback messages", e);
            }
        }
    }


    @Override
    public void savePromptMessages(String namespace, String identifier, ChatMessage message) {
        PromptMessages promptMessages = getPromptChatMessages(namespace, identifier);
        promptMessages.getContent().add(message);
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the prompt messages", e);
        }
    }

    @Override
    public void saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        FeedbackMessages feedbackMessages = getFeedbackChatMessages(namespace, identifier);
        feedbackMessages.getContent().add(message);
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the feedback messages", e);
        }
    }
}
