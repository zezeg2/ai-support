package io.github.zezeg2.aisupport.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedisPromptContextHolder implements PromptContextHolder {

    private final RedisTemplate<String, String> template;
    private final HashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public RedisPromptContextHolder(RedisTemplate<String, String> template, ObjectMapper mapper) {
        this.template = template;
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
    public <T extends MessageContext> T createMessageContext(ContextType contextType, String namespace, String identifier) {
        String[] split = namespace.split(":");
        Long seq = hashOperations.increment(namespace + ":" + identifier, "seq", 1L);

        T messageContext = (T) (contextType == ContextType.PROMPT
                ? PromptMessageContext.builder().seq(seq).functionName(namespace).identifier(identifier).messages(new ArrayList<>()).build()
                : FeedbackMessageContext.builder().seq(seq).functionName(split[0]).validatorName(split[1]).identifier(identifier).messages(new ArrayList<>()).build());
        try {
            hashOperations.put(namespace + ":" + identifier, String.valueOf(seq), mapper.writeValueAsString(messageContext));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the messages", e);
        }
        return messageContext;
    }

    @Override
    public void saveMessageContext(ContextType contextType, MessageContext messageContext) {
        try {
            hashOperations.put(messageContext.getNamespace() + ":" + messageContext.getIdentifier(), String.valueOf(messageContext.getSeq()), mapper.writeValueAsString(messageContext));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the context messages", e);
        }
    }

    @Override
    public void deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n) {
        List<ChatMessage> content = messageContext.getMessages();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
        try {
            hashOperations.put(messageContext.getNamespace() + ":" + messageContext.getIdentifier(), String.valueOf(messageContext.getSeq()), mapper.writeValueAsString(messageContext));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the context messages after deletion", e);
        }
    }
}
