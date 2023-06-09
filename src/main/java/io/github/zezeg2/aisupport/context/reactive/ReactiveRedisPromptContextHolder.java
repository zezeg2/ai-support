package io.github.zezeg2.aisupport.context.reactive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReactiveRedisPromptContextHolder implements ReactivePromptContextHolder {

    private final ReactiveHashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public ReactiveRedisPromptContextHolder(ReactiveStringRedisTemplate template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public Mono<Boolean> contains(String functionName) {
        return hashOperations.hasKey(functionName, "prompt");

    }

    @Override
    public Mono<Void> savePrompt(String functionName, Prompt prompt) {
        String promptJson;
        try {
            promptJson = mapper.writeValueAsString(prompt);
        } catch (Exception e) {
            return Mono.error(handleException("savePrompt", e));
        }

        return hashOperations.put(functionName, "prompt", promptJson)
                .then();

    }

    @Override
    public Mono<Prompt> get(String functionName) {
        return hashOperations.get(functionName, "prompt")
                .flatMap(promptJson -> {
                    try {
                        return Mono.just(mapper.readValue(promptJson, Prompt.class));
                    } catch (Exception e) {
                        return Mono.error(handleException("get", e));
                    }
                });

    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String functionName) {
        return hashOperations.get(functionName, "promptMessagesContext")
                .flatMap(contextJson -> {
                    try {
                        return Mono.just(mapper.readValue(contextJson, new TypeReference<>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getPromptMessagesContext", e));
                    }
                });
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String validatorName) {
        return hashOperations.get(validatorName, "feedbackMessagesContext")
                .flatMap(contextJson -> {
                    try {
                        return Mono.just(mapper.readValue(contextJson, new TypeReference<>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getFeedbackMessagesContext", e));
                    }
                });

    }

    @Override
    public Mono<List<ChatMessage>> getPromptChatMessages(String functionName, String identifier) {
        return hashOperations.get(functionName + ":" + identifier, "promptChatMessages")
                .flatMap(messagesJson -> {
                    try {
                        return Mono.just(mapper.readValue(messagesJson, new TypeReference<>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getPromptChatMessages", e));
                    }
                });

    }

    @Override
    public Mono<List<ChatMessage>> getFeedbackChatMessages(String validatorName, String identifier) {
        return hashOperations.get(validatorName + ":" + identifier, "feedbackChatMessages")
                .flatMap(messagesJson -> {
                    try {
                        return Mono.just(mapper.readValue(messagesJson, new TypeReference<>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getFeedbackChatMessages", e));
                    }
                });

    }

    @Override
    public Mono<Void> savePromptMessagesContext(String functionName, String identifier, ChatMessage message) {
        return hashOperations.get(functionName + ":" + identifier, "promptChatMessages")
                .defaultIfEmpty("[]")
                .flatMap(messagesJson -> {
                    try {
                        List<ChatMessage> messages;
                        if (!messagesJson.isEmpty()) messages = mapper.readValue(messagesJson, new TypeReference<>() {
                        });
                        else messages = new ArrayList<>();
                        messages.add(message);
                        return hashOperations.put(functionName + ":" + identifier, "promptChatMessages", mapper.writeValueAsString(messages));
                    } catch (Exception e) {
                        return Mono.error(handleException("savePromptMessagesContext", e));
                    }
                }).then();
    }

    @Override
    public Mono<Void> saveFeedbackMessagesContext(String validatorName, String identifier, ChatMessage message) {
        return hashOperations.get(validatorName + ":" + identifier, "feedbackChatMessages")
                .defaultIfEmpty("[]")
                .flatMap(messagesJson -> {
                    try {
                        List<ChatMessage> messages;
                        if (messagesJson != null) messages = mapper.readValue(messagesJson, new TypeReference<>() {
                        });
                        else messages = new ArrayList<>();
                        messages.add(message);
                        return hashOperations.put(validatorName + ":" + identifier, "feedbackChatMessages", mapper.writeValueAsString(messages));
                    } catch (Exception e) {
                        return Mono.error(handleException("saveFeedbackMessagesContext", e));
                    }
                })
                .then();

    }

    private RuntimeException handleException(String methodName, Exception exception) {
        log.info("Exception Occurred \n- name : {}\n-message: {}", exception.getClass().getSimpleName(), exception.getMessage());
        return new RuntimeException("Error occurred in method: " + methodName, exception);
    }
}

