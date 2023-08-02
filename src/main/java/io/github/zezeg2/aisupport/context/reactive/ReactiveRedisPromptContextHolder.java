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
    private final ReactiveRedisTemplate<String, String> template;
    private final ReactiveHashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public ReactiveRedisPromptContextHolder(ReactiveRedisTemplate<String, String> template, ObjectMapper mapper) {
        this.template = template;
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
    public <T extends MessageContext> Mono<T> createMessageContext(ContextType contextType, String namespace, String identifier) {
        return Mono.just(namespace)
                .map(n -> n.split(":"))
                .zipWith(hashOperations.increment(namespace + ":" + identifier, "seq", 1L),
                        (split, seq) -> {
                            T messageContext = (T) (contextType == ContextType.PROMPT
                                    ? PromptMessageContext.builder().seq(seq).functionName(namespace).identifier(identifier).messages(new ArrayList<>()).build()
                                    : FeedbackMessageContext.builder().seq(seq).functionName(split[0]).validatorName(split[1]).identifier(identifier).messages(new ArrayList<>()).build());
                            try {
                                hashOperations.put(namespace + ":" + identifier, String.valueOf(seq), mapper.writeValueAsString(messageContext));
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("Error serializing the messages", e);
                            }
                            return messageContext;
                        });
    }

    @Override
    public Mono<Void> saveMessageContext(ContextType contextType, MessageContext messageContext) {
        try {
            return hashOperations.put(messageContext.getNamespace() + ":" + messageContext.getIdentifier(), String.valueOf(messageContext.getSeq()), mapper.writeValueAsString(messageContext)).then();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing the context messages", e);
        }
    }

    @Override
    public Mono<Void> deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n) {
        return Mono.defer(() -> {
            List<ChatMessage> messageList = messageContext.getMessages();
            int removeIndex = Math.max(0, messageList.size() - n);
            messageList.subList(removeIndex, messageList.size()).clear();
            try {
                return hashOperations.put(messageContext.getNamespace() + ":" + messageContext.getIdentifier(), String.valueOf(messageContext.getSeq()), mapper.writeValueAsString(messageContext)).then();
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException("Error serializing the messages after deletion", e));
            }
        });
    }
}

