package io.github.zezeg2.aisupport.context.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class ReactiveRedisPromptContextHolder implements ReactivePromptContextHolder {
    private final ReactiveHashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public ReactiveRedisPromptContextHolder(ReactiveRedisTemplate<String, String> template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public Mono<Boolean> containsPrompt(String functionName) {
        return hashOperations.hasKey("prompts", functionName);
    }

    @Override
    public Mono<Void> savePromptToContext(String functionName, Prompt prompt) {
        try {
            String promptData = mapper.writeValueAsString(prompt);
            return hashOperations.put("prompts", functionName, promptData)
                    .then();
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Error serializing prompt", e));
        }
    }

    @Override
    public Mono<Prompt> getPrompt(String functionName) {
        return hashOperations.get("prompts", functionName)
                .map(promptData -> {
                    try {
                        return mapper.readValue(promptData, Prompt.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Error deserializing prompt", e);
                    }
                });
    }
}
