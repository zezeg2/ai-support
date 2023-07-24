package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReactiveLocalMemoryPromptContextHolder implements ReactivePromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<ContextType, Map<String, CopyOnWriteArrayList<MessageContext>>> contextRegistry =
            new ConcurrentHashMap<>(Map.of(ContextType.PROMPT, new ConcurrentHashMap<>(), ContextType.FEEDBACK, new ConcurrentHashMap<>()));

    @Override
    public Mono<Boolean> contains(String namespace) {
        return Mono.just(promptRegistry.containsKey(namespace));
    }

    @Override
    public Mono<Void> savePrompt(String namespace, Prompt prompt) {
        return Mono.fromRunnable(() -> promptRegistry.put(namespace, prompt));
    }

    @Override
    public Mono<Prompt> get(String namespace) {
        return Mono.justOrEmpty(promptRegistry.get(namespace));
    }

    @Override
    public <T extends MessageContext> Mono<T> getContext(ContextType contextType, String namespace, String identifier) {
        return Mono.defer(() -> {
            Map<String, CopyOnWriteArrayList<MessageContext>> selectedRegistry = contextRegistry.get(contextType);
            if (!selectedRegistry.containsKey(namespace)) selectedRegistry.put(namespace, new CopyOnWriteArrayList<>());
            return Mono.just((T) selectedRegistry.get(namespace).stream()
                    .filter(context -> context.getIdentifier().equals(identifier)).findFirst()
                    .orElseGet(() -> {
                        T context = contextType == ContextType.PROMPT ? (T) PromptMessageContext.builder().identifier(identifier).messages(new CopyOnWriteArrayList<>()).build()
                                : (T) FeedbackMessageContext.builder().identifier(identifier).messages(new CopyOnWriteArrayList<>()).build();
                        selectedRegistry.get(namespace).add(context);
                        return context;
                    }));
        });
    }

    @Override
    public Mono<Void> saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message) {
        return Mono.defer(() -> getContext(contextType, namespace, identifier).flatMap(messageContext -> {
            messageContext.getMessages().add(message);
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> saveContext(ContextType contextType, MessageContext messageContext) {
        return Mono.defer(() -> {
            String namespace = contextType == ContextType.PROMPT ? messageContext.getFunctionName() : messageContext.getFunctionName() + ":" + ((FeedbackMessageContext) messageContext).getValidatorName();
            MessageContext origin = contextRegistry.get(contextType).get(namespace).stream()
                    .filter(ctx -> ctx.getIdentifier().equals(messageContext.getIdentifier()))
                    .findFirst().orElseThrow();
            origin.setMessages(messageContext.getMessages());
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n) {
        return Mono.defer(() -> {
            List<MessageContext> messageContextList = contextRegistry.get(contextType).get(namespace);
            if (messageContextList != null) {
                Optional<MessageContext> messageContextOptional = messageContextList.stream()
                        .filter(messageContext -> messageContext.getIdentifier().equals(identifier)).findFirst();
                messageContextOptional.ifPresent(messageContext -> {
                    List<ChatMessage> content = messageContext.getMessages();
                    if (!content.isEmpty()) {
                        int removeIndex = Math.max(0, content.size() - n);
                        content.subList(removeIndex, content.size()).clear();
                    }
                });
            }
            return Mono.empty();
        });
    }
}
