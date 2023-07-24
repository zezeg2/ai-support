package io.github.zezeg2.aisupport.context.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;
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
    public <T extends MessageContext> Mono<T> getContext(ContextType contextType, String namespace, String identifier) {
        return hashOperations.get(namespace, identifier)
                .flatMap(serializedMessages -> {
                    try {
                        return Mono.just((T) mapper.readValue(serializedMessages, contextType.getContextClass()));
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Error deserializing the messages", e));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    T context;
                    if (contextType == ContextType.PROMPT) {
                        context = (T) PromptMessageContext.builder().identifier(identifier).functionName(namespace).messages(new ArrayList<>()).build();
                    } else {
                        String[] split = namespace.split(":");
                        context = (T) FeedbackMessageContext.builder().identifier(identifier).functionName(split[0]).validatorName(split[1]).messages(new ArrayList<>()).build();
                    }
                    try {
                        hashOperations.put(namespace, identifier, mapper.writeValueAsString(context));
                        return Mono.just(context);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the messages", e));
                    }
                }));
    }

    @Override
    public Mono<Void> saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message) {
        return getContext(contextType, namespace, identifier)
                .flatMap(contextMessages -> {
                    contextMessages.getMessages().add(message);
                    try {
                        return hashOperations.put(namespace, identifier, mapper.writeValueAsString(contextMessages)).then();
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the messages", e));
                    }
                });
    }

    @Override
    public Mono<Void> saveContext(ContextType contextType, MessageContext messageContext) {
        try {
            String namespace;
            if (contextType == ContextType.PROMPT) {
                namespace = messageContext.getFunctionName();
            } else {
                FeedbackMessageContext feedbackContext = (FeedbackMessageContext) messageContext;
                namespace = feedbackContext.getFunctionName() + ":" + feedbackContext.getValidatorName();
            }
            return hashOperations.put(namespace, messageContext.getIdentifier(), mapper.writeValueAsString(messageContext)).then();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the context messages", e);
        }
    }

    @Override
    public Mono<Void> deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n) {
        return getContext(contextType, namespace, identifier)
                .filter(contextMessages -> !contextMessages.getMessages().isEmpty())
                .doOnNext(contextMessages -> {
                    List<ChatMessage> content = contextMessages.getMessages();
                    int removeIndex = Math.max(0, content.size() - n);
                    content.subList(removeIndex, content.size()).clear();
                })
                .flatMap(contextMessages -> {
                    try {
                        return hashOperations.put(namespace, identifier, mapper.writeValueAsString(contextMessages)).then();
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error serializing the messages after deletion", e));
                    }
                });
    }
}

