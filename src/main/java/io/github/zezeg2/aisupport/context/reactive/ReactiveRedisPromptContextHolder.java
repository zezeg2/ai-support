package io.github.zezeg2.aisupport.context.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessageContext;
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
    public Mono<PromptMessageContext> getPromptChatMessages(String namespace, String identifier) {
        return hashOperations.get(namespace, identifier)
                .flatMap(serializedPromptMessages -> {
                    try {
                        return Mono.just(mapper.readValue(serializedPromptMessages, PromptMessageContext.class));
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Error deserializing the prompt messages", e));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    PromptMessageContext promptContext = PromptMessageContext.builder()
                            .identifier(identifier)
                            .functionName(namespace)
                            .messages(new ArrayList<>()).build();
                    try {
                        hashOperations.put(namespace, identifier, mapper.writeValueAsString(promptContext));
                        return Mono.just(promptContext);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the prompt messages", e));
                    }
                }));
    }

    @Override
    public Mono<FeedbackMessageContext> getFeedbackChatMessages(String namespace, String identifier) {
        String[] split = namespace.split(":");
        return hashOperations.get(namespace, identifier)
                .flatMap(serializedFeedbackMessages -> {
                    try {
                        return Mono.just(mapper.readValue(serializedFeedbackMessages, FeedbackMessageContext.class));
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Error deserializing the feedback messages", e));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    FeedbackMessageContext feedbackContext = FeedbackMessageContext.builder()
                            .identifier(identifier)
                            .functionName(split[0])
                            .validatorName(split[1])
                            .messages(new ArrayList<>()).build();
                    try {
                        hashOperations.put(namespace, identifier, mapper.writeValueAsString(feedbackContext));
                        return Mono.just(feedbackContext);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the feedback messages", e));
                    }
                }));
    }

    @Override
    public Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message) {
        return getPromptChatMessages(namespace, identifier)
                .doOnNext(promptMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && promptMessages.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
                        promptMessages.getMessages().get(0).setContent(message.getContent());
                    } else {
                        promptMessages.getMessages().add(message);
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
    public Mono<Void> savePromptMessages(PromptMessageContext messages) {
        try {
            return hashOperations.put(messages.getFunctionName(), messages.getIdentifier(), mapper.writeValueAsString(messages)).then();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        return getFeedbackChatMessages(namespace, identifier)
                .doOnNext(feedbackMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && feedbackMessages.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
                        feedbackMessages.getMessages().get(0).setContent(message.getContent());
                    } else {
                        feedbackMessages.getMessages().add(message);
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
    public Mono<Void> saveFeedbackMessages(FeedbackMessageContext messages) {
        try {
            return hashOperations.put(messages.getFunctionName() + ":" + messages.getValidatorName(), messages.getIdentifier(), mapper.writeValueAsString(messages)).then();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> deleteLastPromptMessage(String namespace, String identifier, Integer n) {
        return getPromptChatMessages(namespace, identifier)
                .filter(promptMessages -> !promptMessages.getMessages().isEmpty())
                .doOnNext(promptMessages -> {
                    List<ChatMessage> content = promptMessages.getMessages();
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
                .filter(feedbackMessages -> !feedbackMessages.getMessages().isEmpty())
                .doOnNext(feedbackMessages -> {
                    List<ChatMessage> content = feedbackMessages.getMessages();
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

