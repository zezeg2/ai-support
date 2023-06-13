package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReactiveLocalMemoryPromptContextHolder implements ReactivePromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<ChatMessage>>> promptMessagesRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<ChatMessage>>> feedbackMessagesRegistry = new ConcurrentHashMap<>();


    @Override
    public Mono<Boolean> contains(String namespace) {
        return null;
    }

    @Override
    public Mono<Void> savePrompt(String namespace, Prompt prompt) {
        return null;
    }

    @Override
    public Mono<Prompt> get(String namespace) {
        return null;
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String namespace) {
        return null;
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String namespace) {
        return null;
    }

    @Override
    public Mono<List<ChatMessage>> getPromptChatMessages(String namespace, String identifier) {
        return null;
    }

    @Override
    public Mono<List<ChatMessage>> getFeedbackChatMessages(String namespace, String identifier) {
        return null;
    }

    @Override
    public Mono<Void> savePromptMessagesContext(String namespace, String identifier, ChatMessage message) {
        return null;
    }

    @Override
    public Mono<Void> saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message) {
        return null;
    }
}
