package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessageContext;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReactiveLocalMemoryPromptContextHolder implements ReactivePromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<String, List<PromptMessageContext>> promptMessagesRegistry = new ConcurrentHashMap<>();
    private static final Map<String, List<FeedbackMessageContext>> feedbackMessagesRegistry = new ConcurrentHashMap<>();

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
    public Mono<PromptMessageContext> getPromptChatMessages(String namespace, String identifier) {
        return Mono.justOrEmpty(promptMessagesRegistry.get(namespace).stream()
                .filter(promptMessages -> promptMessages.getIdentifier().equals(identifier)).findFirst()
                .orElse(PromptMessageContext.builder().identifier(identifier).messages(new CopyOnWriteArrayList<>()).build()));
    }

    @Override
    public Mono<FeedbackMessageContext> getFeedbackChatMessages(String namespace, String identifier) {
        return Mono.justOrEmpty(feedbackMessagesRegistry.get(namespace).stream()
                .filter(feedbackMessages -> feedbackMessages.getIdentifier().equals(identifier)).findFirst()
                .orElse(FeedbackMessageContext.builder().identifier(identifier).messages(new CopyOnWriteArrayList<>()).build()));
    }

    @Override
    public Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message) {
        return Mono.fromRunnable(() -> {
                    if (!promptMessagesRegistry.containsKey(namespace)) {
                        promptMessagesRegistry.put(namespace, new CopyOnWriteArrayList<>());
                    }
                }).then(getPromptChatMessages(namespace, identifier))
                .flatMap(promptMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && promptMessages.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
                        promptMessages.getMessages().get(0).setContent(message.getContent());
                    } else {
                        promptMessages.getMessages().add(message);
                    }
                    return Mono.just(promptMessages);
                })
                .flatMap(promptMessages -> {
                    if (promptMessagesRegistry.get(namespace).stream()
                            .noneMatch(existingPromptMessages -> existingPromptMessages.getIdentifier().equals(identifier))) {
                        promptMessagesRegistry.get(namespace).add(promptMessages);
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> savePromptMessages(PromptMessageContext messages) {
        return getPromptChatMessages(messages.getFunctionName(), messages.getIdentifier())
                .flatMap(promptMessages -> {
                    promptMessages.setMessages(messages.getMessages());
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
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && feedbackMessages.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
                        feedbackMessages.getMessages().get(0).setContent(message.getContent());
                    } else {
                        feedbackMessages.getMessages().add(message);
                    }
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

    @Override
    public Mono<Void> saveFeedbackMessages(FeedbackMessageContext messages) {
        return getFeedbackChatMessages(messages.getFunctionName() + ":" + messages.getValidatorName(), messages.getIdentifier())
                .flatMap(promptMessages -> {
                    promptMessages.setMessages(messages.getMessages());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> deleteLastPromptMessage(String namespace, String identifier, Integer n) {
        return Mono.fromRunnable(() -> {
            List<PromptMessageContext> promptContextList = promptMessagesRegistry.get(namespace);
            if (promptContextList != null) {
                Optional<PromptMessageContext> promptMessagesOptional = promptContextList.stream()
                        .filter(promptMessages -> promptMessages.getIdentifier().equals(identifier)).findFirst();
                promptMessagesOptional.ifPresent(promptMessages -> {
                    List<ChatMessage> content = promptMessages.getMessages();
                    if (!content.isEmpty()) {
                        int removeIndex = Math.max(0, content.size() - n);
                        content.subList(removeIndex, content.size()).clear();
                    }
                });
            }
        });
    }

    @Override
    public Mono<Void> deleteLastFeedbackMessage(String namespace, String identifier, Integer n) {
        return Mono.fromRunnable(() -> {
            List<FeedbackMessageContext> feedbackContextList = feedbackMessagesRegistry.get(namespace);
            if (feedbackContextList != null) {
                Optional<FeedbackMessageContext> feedbackMessagesOptional = feedbackContextList.stream()
                        .filter(feedbackMessages -> feedbackMessages.getIdentifier().equals(identifier)).findFirst();
                feedbackMessagesOptional.ifPresent(feedbackMessages -> {
                    List<ChatMessage> content = feedbackMessages.getMessages();
                    if (!content.isEmpty()) {
                        int removeIndex = Math.max(0, content.size() - n);
                        content.subList(removeIndex, content.size()).clear();
                    }
                });
            }
        });
    }
}
