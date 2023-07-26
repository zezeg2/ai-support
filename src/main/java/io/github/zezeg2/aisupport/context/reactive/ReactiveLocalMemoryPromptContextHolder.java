package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
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
    public <T extends MessageContext> Mono<T> createMessageContext(ContextType contextType, String namespace, String identifier) {
        return Mono.defer(() -> {
            Map<String, CopyOnWriteArrayList<MessageContext>> selectedRegistry = contextRegistry.get(contextType);
            if (!selectedRegistry.containsKey(namespace)) selectedRegistry.put(namespace, new CopyOnWriteArrayList<>());
            String[] split = namespace.split(":");
            CopyOnWriteArrayList<MessageContext> messageContextList = selectedRegistry.get(identifier);
            Long seq = (long) messageContextList.size();

            T messageContext = (T) (contextType == ContextType.PROMPT
                    ? PromptMessageContext.builder().seq(seq).functionName(namespace).identifier(identifier).messages(new CopyOnWriteArrayList<>()).build()
                    : FeedbackMessageContext.builder().seq(seq).functionName(split[0]).validatorName(split[1]).identifier(identifier).messages(new CopyOnWriteArrayList<>()).build());

            messageContextList.add(messageContext);
            return Mono.just(messageContext);
        });
    }

    @Override
    public Mono<Void> saveMessageContext(ContextType contextType, MessageContext messageContext) {
        return Mono.defer(() -> {
            MessageContext origin = contextRegistry.get(contextType).get(messageContext.getNamespace()).stream()
                    .filter(ctx -> ctx.getIdentifier().equals(messageContext.getIdentifier()))
                    .findFirst().orElseThrow();
            origin.setMessages(messageContext.getMessages());
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n) {
        return Mono.defer(() -> {
            List<ChatMessage> messageList = messageContext.getMessages();
            if (!messageList.isEmpty()) {
                int removeIndex = Math.max(0, messageList.size() - n);
                messageList.subList(removeIndex, messageList.size()).clear();
            }
            return Mono.empty();
        });
    }
}
