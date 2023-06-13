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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ReactiveRedisPromptContextHolder implements ReactivePromptContextHolder {

    private final ReactiveHashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public ReactiveRedisPromptContextHolder(ReactiveStringRedisTemplate template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public Mono<Boolean> contains(String namespace) {
        return hashOperations.hasKey(namespace, "prompt");
    }

    @Override
    public Mono<Void> savePrompt(String namespace, Prompt prompt) {
        String promptJson;
        try {
            promptJson = mapper.writeValueAsString(prompt);
        } catch (Exception e) {
            return Mono.error(handleException("savePrompt", e));
        }

        return hashOperations.put(namespace, "prompt", promptJson)
                .then();

    }

    @Override
    public Mono<Prompt> get(String namespace) {
        return hashOperations.get(namespace, "prompt")
                .flatMap(promptJson -> {
                    try {
                        return Mono.just(mapper.readValue(promptJson, Prompt.class));
                    } catch (Exception e) {
                        return Mono.error(handleException("get", e));
                    }
                });

    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String namespace) {
        return hashOperations.get(namespace, "promptMessagesContext")
                .flatMap(contextJson -> {
                    try {
                        return Mono.just(mapper.readValue(contextJson, new TypeReference<Map<String, List<ChatMessage>>>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getPromptMessagesContext", e));
                    }
                }).switchIfEmpty(Mono.<Map<String, List<ChatMessage>>>just(new ConcurrentHashMap<>()));
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String namespace) {
        return hashOperations.get(namespace, "feedbackMessagesContext")
                .flatMap(contextJson -> {
                    try {
                        return Mono.just(mapper.readValue(contextJson, new TypeReference<Map<String, List<ChatMessage>>>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getFeedbackMessagesContext", e));
                    }
                }).switchIfEmpty(Mono.<Map<String, List<ChatMessage>>>just(new ConcurrentHashMap<>()));

    }

    @Override
    public Mono<List<ChatMessage>> getPromptChatMessages(String namespace, String identifier) {
        return hashOperations.get(namespace, "promptChatMessages:" + identifier)
                .flatMap(messagesJson -> {
                    try {
                        return Mono.just(mapper.readValue(messagesJson, new TypeReference<List<ChatMessage>>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getPromptChatMessages", e));
                    }
                })
                .switchIfEmpty(Mono.<List<ChatMessage>>just(new ArrayList<>()));

    }

    @Override
    public Mono<List<ChatMessage>> getFeedbackChatMessages(String namespace, String identifier) {
        return hashOperations.get(namespace, "feedbackChatMessages:" + identifier)
                .flatMap(messagesJson -> {
                    try {
                        return Mono.just(mapper.readValue(messagesJson, new TypeReference<List<ChatMessage>>() {
                        }));
                    } catch (Exception e) {
                        return Mono.error(handleException("getFeedbackChatMessages", e));
                    }
                })
                .switchIfEmpty(Mono.<List<ChatMessage>>just(new ArrayList<>()));

    }

    @Override
    public Mono<Void> savePromptMessagesContext(String namespace, String identifier, ChatMessage message) {
        return hashOperations.get(namespace, "promptChatMessages:" + identifier)
                .defaultIfEmpty("[]")
                .flatMap(messagesJson -> {
                    try {
                        List<ChatMessage> messages;
                        if (!messagesJson.isEmpty()) messages = mapper.readValue(messagesJson, new TypeReference<>() {
                        });
                        else messages = new ArrayList<>();
                        messages.add(message);
                        return hashOperations.put(namespace, "promptChatMessages:" + identifier, mapper.writeValueAsString(messages));
                    } catch (Exception e) {
                        return Mono.error(handleException("savePromptMessagesContext", e));
                    }
                }).then();
    }

    @Override
    public Mono<Void> saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message) {
        return hashOperations.get(namespace, "feedbackChatMessages:" + identifier)
                .defaultIfEmpty("[]")
                .flatMap(messagesJson -> {
                    try {
                        List<ChatMessage> messages;
                        if (messagesJson != null) messages = mapper.readValue(messagesJson, new TypeReference<>() {
                        });
                        else messages = new ArrayList<>();
                        messages.add(message);
                        return hashOperations.put(namespace, "feedbackChatMessages:" + identifier, mapper.writeValueAsString(messages));
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

