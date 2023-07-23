package io.github.zezeg2.aisupport.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.*;
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
    public <T extends MessageContext> T getContext(ContextType contextType, String namespace, String identifier) {
        String result = hashOperations.get(namespace, identifier);
        if (result == null) {
            String[] split = namespace.split(":");
            T context = contextType == ContextType.PROMPT ? (T) PromptMessageContext.builder().functionName(namespace).identifier(identifier).messages(new CopyOnWriteArrayList<>()).build()
                    : (T) FeedbackMessageContext.builder().identifier(identifier).functionName(split[0]).validatorName(split[1]).messages(new ArrayList<>()).build();
            saveContext(contextType, context);
            return context;
        } else {
            try {
                return (T) mapper.readValue(result, contextType.getContextClass());
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing the messages", e);
            }
        }
    }

    @Override
    public void saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message) {
        MessageContext messageContext = getContext(contextType, namespace, identifier);
        if (message.getRole().equals(ROLE.SYSTEM.getValue()) && messageContext.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
            messageContext.getMessages().get(0).setContent(message.getContent());
        } else {
            messageContext.getMessages().add(message);
        }
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(messageContext));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the messages", e);
        }
    }

    @Override
    public void saveContext(ContextType contextType, MessageContext messageContext) {
        String key = contextType == ContextType.PROMPT ? messageContext.getFunctionName() : messageContext.getFunctionName() + ":" + ((FeedbackMessageContext) messageContext).getValidatorName();
        try {
            hashOperations.put(key, messageContext.getIdentifier(), mapper.writeValueAsString(messageContext));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the context messages", e);
        }
    }

    @Override
    public void deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n) {
        MessageContext messageContext = getContext(contextType, namespace, identifier);
        List<ChatMessage> content = messageContext.getMessages();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
        try {
            hashOperations.put(namespace, identifier, mapper.writeValueAsString(messageContext));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the context messages after deletion", e);
        }
    }
}
