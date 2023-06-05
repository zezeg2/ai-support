package io.github.zezeg2.aisupport.context.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

public class RedisPromptContextHolder implements PromptContextHolder {
    private final HashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public RedisPromptContextHolder(RedisTemplate<String, String> template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public boolean containsPrompt(String functionName) {
        return hashOperations.hasKey("prompts", functionName);
    }

    @Override
    public void savePromptToContext(String functionName, Prompt prompt) {
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
}
