package io.github.zezeg2.aisupport.context.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class ReactiveRedisPromptContextHolder<S> implements ReactivePromptContextHolder<S> {
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
    public Mono<Void> savePromptToContext(String functionName, ReactivePrompt<S> prompt) {
        try {
            String promptData = mapper.writeValueAsString(prompt);
            return hashOperations.put("prompts", functionName, promptData).then();
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Error serializing prompt", e));
        }
    }

    @Override
    public Mono<ReactivePrompt<S>> getPrompt(String functionName) {
        return hashOperations.get("prompts", functionName)
                .log().map(promptData -> {
                    try {
                        ReactivePrompt reactivePrompt = mapper.readValue(promptData, ReactivePrompt.class);
                        return (ReactivePrompt<S>) reactivePrompt;
                    } catch (IOException e) {
                        throw new RuntimeException("Error deserializing prompt", e);
                    }
                }).log();
    }
}
