package io.github.zezeg2.aisupport.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisPromptContextHolder implements PromptContextHolder {
    private final HashOperations<String, String, String> hashOperations;
    private final ContextIdentifierProvider identifierProvider;
    private final ObjectMapper mapper;

    public RedisPromptContextHolder(RedisTemplate<String, String> template, ContextIdentifierProvider identifierProvider, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.identifierProvider = identifierProvider;
        this.mapper = mapper;
    }

    @Override
    public boolean containsPrompt(String functionName) {
        return hashOperations.hasKey("prompts", functionName);
    }

    @Override
    public void addPromptToContext(String functionName, Prompt prompt) {
        try {
            hashOperations.put("prompts", functionName, mapper.writeValueAsString(prompt));
        } catch (IOException e) {
            throw new RuntimeException("Error serializing prompt", e);
        }
    }

    @Override
    public Prompt getPrompt(String functionName) {
        try {
            String promptData = hashOperations.get("prompts", functionName);
            return mapper.readValue(promptData, Prompt.class);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing prompt", e);
        }
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessageContext(String functionName) {
        try {
            String messageContextData = hashOperations.get("promptMessageContexts", functionName);
            TypeReference<HashMap<String, List<ChatMessage>>> typeRef = new TypeReference<>() {
            };
            return mapper.readValue(messageContextData, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing message context", e);
        }
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackAssistantMessageContext(String functionName) {
        try {
            String assistantContextData = hashOperations.get("feedbackAssistantContexts", functionName);
            TypeReference<HashMap<String, List<ChatMessage>>> typeRef = new TypeReference<>() {
            };
            return mapper.readValue(assistantContextData, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing assistant context", e);
        }
    }
}
