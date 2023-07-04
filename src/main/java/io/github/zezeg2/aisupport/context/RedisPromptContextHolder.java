package io.github.zezeg2.aisupport.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        if (serializedPrompt == null) return null;
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
            PromptMessages promptMessages = PromptMessages.builder().functionName(namespace).identifier(identifier).content(new CopyOnWriteArrayList<>()).build();
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
        String[] split = namespace.split(":");
        String serializedFeedbackMessages = hashOperations.get(namespace, identifier);
        if (serializedFeedbackMessages == null) {
            FeedbackMessages feedbackMessages = FeedbackMessages.builder()
                    .identifier(identifier)
                    .functionName(split[0])
                    .validatorName(split[1])
                    .content(new ArrayList<>()).build();
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
        if (message.getRole().equals(ROLE.SYSTEM.getValue()) && promptMessages.getContent().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
            promptMessages.getContent().get(0).setContent(message.getContent());
        } else {
            promptMessages.getContent().add(message);
        }
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the prompt messages", e);
        }
    }

    @Override
    public void savePromptMessages(PromptMessages messages) {
        try {
            hashOperations.put(messages.getFunctionName(), messages.getIdentifier(), mapper.writeValueAsString(messages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the prompt messages", e);
        }
    }

    @Override
    public void saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        FeedbackMessages feedbackMessages = getFeedbackChatMessages(namespace, identifier);
        if (message.getRole().equals(ROLE.SYSTEM.getValue()) && feedbackMessages.getContent().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
            feedbackMessages.getContent().get(0).setContent(message.getContent());
        } else {
            feedbackMessages.getContent().add(message);
        }
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the feedback messages", e);
        }
    }

    @Override
    public void saveFeedbackMessages(FeedbackMessages messages) {
        try {
            hashOperations.put(messages.getFunctionName() + ":" + messages.getValidatorName(), messages.getIdentifier(), mapper.writeValueAsString(messages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the prompt messages", e);
        }
    }

    @Override
    public void deleteLastPromptMessage(String namespace, String identifier, Integer n) {
        PromptMessages promptMessages = getPromptChatMessages(namespace, identifier);
        List<ChatMessage> content = promptMessages.getContent();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the prompt messages after deletion");
        }
    }

    @Override
    public void deleteLastFeedbackMessage(String namespace, String identifier, Integer n) {
        FeedbackMessages feedbackMessages = getFeedbackChatMessages(namespace, identifier);
        List<ChatMessage> content = feedbackMessages.getContent();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the feedback messages after deletion");
        }
    }
}
