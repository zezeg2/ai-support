package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReactiveLocalMemoryPromptContextHolder implements ReactivePromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<String, List<PromptMessages>> promptMessagesRegistry = new ConcurrentHashMap<>();
    private static final Map<String, List<FeedbackMessages>> feedbackMessagesRegistry = new ConcurrentHashMap<>();

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
    public Mono<PromptMessages> getPromptChatMessages(String namespace, String identifier) {
        return Mono.justOrEmpty(promptMessagesRegistry.get(namespace).stream()
                .filter(promptMessages -> promptMessages.getIdentifier().equals(identifier)).findFirst()
                .orElse(PromptMessages.builder().identifier(identifier).content(new CopyOnWriteArrayList<>()).build()));
    }

    @Override
    public Mono<FeedbackMessages> getFeedbackChatMessages(String namespace, String identifier) {
        return Mono.justOrEmpty(feedbackMessagesRegistry.get(namespace).stream()
                .filter(feedbackMessages -> feedbackMessages.getIdentifier().equals(identifier)).findFirst()
                .orElse(FeedbackMessages.builder().identifier(identifier).content(new CopyOnWriteArrayList<>()).build()));
    }

    @Override
    public Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message) {
        return Mono.fromRunnable(() -> {
                    if (!promptMessagesRegistry.containsKey(namespace)) {
                        promptMessagesRegistry.put(namespace, new CopyOnWriteArrayList<>());
                    }
                }).then(getPromptChatMessages(namespace, identifier))
                .flatMap(promptChatMessages -> {
                    promptChatMessages.getContent().add(message);
                    return Mono.just(promptChatMessages);
                })
                .flatMap(promptChatMessages -> {
                    if (promptMessagesRegistry.get(namespace).stream()
                            .noneMatch(existingPromptMessages -> existingPromptMessages.getIdentifier().equals(identifier))) {
                        promptMessagesRegistry.get(namespace).add(promptChatMessages);
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        return Mono.fromRunnable(() -> {
                    if (!feedbackMessagesRegistry.containsKey(namespace)) {
                        feedbackMessagesRegistry.put(namespace, new CopyOnWriteArrayList<>());
                    }
                }).then(getFeedbackChatMessages(namespace, identifier))
                .flatMap(feedbackMessages -> {
                    feedbackMessages.getContent().add(message);
                    return Mono.just(feedbackMessages);
                })
                .flatMap(feedbackMessages -> {
                    if (feedbackMessagesRegistry.get(namespace).stream()
                            .noneMatch(existingFeedbackMessages -> existingFeedbackMessages.getIdentifier().equals(identifier))) {
                        feedbackMessagesRegistry.get(namespace).add(feedbackMessages);
                    }
                    return Mono.empty();
                });
    }

}
