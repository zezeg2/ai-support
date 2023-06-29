package io.github.zezeg2.aisupport.context.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReactiveRedisPromptContextHolder implements ReactivePromptContextHolder {

    private final ReactiveHashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public ReactiveRedisPromptContextHolder(ReactiveRedisTemplate<String, String> template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public Mono<Boolean> contains(String namespace) {
        return hashOperations.hasKey(namespace, "prompt");
    }

    @Override
    public Mono<Void> savePrompt(String namespace, Prompt prompt) {
        try {
            String serializedPrompt = mapper.writeValueAsString(prompt);
            return hashOperations.put(namespace, "prompt", serializedPrompt).then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing the prompt", e));
        }
    }

    @Override
    public Mono<Prompt> get(String namespace) {
        return hashOperations.get(namespace, "prompt")
                .handle((serializedPrompt, sink) -> {
                    try {
                        sink.next(mapper.readValue(serializedPrompt, Prompt.class));
                    } catch (IOException e) {
                        sink.error(new RuntimeException("Error deserializing the prompt", e));
                    }
                });
    }

    @Override
    public Mono<PromptMessages> getPromptChatMessages(String namespace, String identifier) {
        return hashOperations.get(namespace, identifier)
                .flatMap(serializedPromptMessages -> {
                    try {
                        return Mono.just(mapper.readValue(serializedPromptMessages, PromptMessages.class));
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Error deserializing the prompt messages", e));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    PromptMessages promptMessages = PromptMessages.builder()
                            .identifier(identifier)
                            .functionName(namespace)
                            .content(new ArrayList<>()).build();
                    try {
                        hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages));
                        return Mono.just(promptMessages);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the prompt messages", e));
                    }
                }));
    }

    @Override
    public Mono<FeedbackMessages> getFeedbackChatMessages(String namespace, String identifier) {
        String[] split = namespace.split(":");
        return hashOperations.get(namespace, identifier)
                .flatMap(serializedFeedbackMessages -> {
                    try {
                        return Mono.just(mapper.readValue(serializedFeedbackMessages, FeedbackMessages.class));
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Error deserializing the feedback messages", e));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    FeedbackMessages feedbackMessages = FeedbackMessages.builder()
                            .identifier(identifier)
                            .functionName(split[0])
                            .validatorName(split[1])
                            .content(new ArrayList<>()).build();
                    try {
                        hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages));
                        return Mono.just(feedbackMessages);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the feedback messages", e));
                    }
                }));
    }

    @Override
    public Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message) {
        return getPromptChatMessages(namespace, identifier)
                .doOnNext(promptMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && promptMessages.getContent().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))){
                        promptMessages.getContent().get(0).setContent(message.getContent());
                    } else {
                        promptMessages.getContent().add(message);
                    }
                })
                .flatMap(promptMessages -> {
                    try {
                        return hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages)).then();
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the prompt messages", e));
                    }
                });
    }

    @Override
    public Mono<Void> saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        return getFeedbackChatMessages(namespace, identifier)
                .doOnNext(feedbackMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && feedbackMessages.getContent().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))){
                        feedbackMessages.getContent().get(0).setContent(message.getContent());
                    } else {
                        feedbackMessages.getContent().add(message);
                    }
                })
                .flatMap(feedbackMessages -> {
                    try {
                        return hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages)).then();
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the feedback messages", e));
                    }
                });
    }

    @Override
    public Mono<Void> deleteLastPromptMessage(String namespace, String identifier, Integer n) {
        return getPromptChatMessages(namespace, identifier)
                .filter(promptMessages -> !promptMessages.getContent().isEmpty())
                .doOnNext(promptMessages -> {
                    List<ChatMessage> content = promptMessages.getContent();
                    if (!content.isEmpty()) {
                        int removeIndex = Math.max(0, content.size() - n);
                        content.subList(removeIndex, content.size()).clear();
                    }
                })
                .flatMap(promptMessages -> {
                    try {
                        return hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptMessages)).then();
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the prompt messages after deletion", e));
                    }
                });
    }

    @Override
    public Mono<Void> deleteLastFeedbackMessage(String namespace, String identifier, Integer n) {
        return getFeedbackChatMessages(namespace, identifier)
                .filter(feedbackMessages -> !feedbackMessages.getContent().isEmpty())
                .doOnNext(feedbackMessages -> {
                    List<ChatMessage> content = feedbackMessages.getContent();
                    int removeIndex = Math.max(0, content.size() - n);
                    content.subList(removeIndex, content.size()).clear();
                })
                .flatMap(feedbackMessages -> {
                    try {
                        return hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackMessages)).then();
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the feedback messages after deletion", e));
                    }
                });
    }

}

