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
    public Mono<Boolean> contains(String functionName) {
        return null;
    }

    @Override
    public Mono<Void> savePrompt(String functionName, Prompt prompt) {
        return null;
    }

    @Override
    public Mono<Prompt> get(String functionName) {
        return null;
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String functionName) {
        return null;
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String validatorName) {
        return null;
    }

    @Override
    public Mono<List<ChatMessage>> getPromptChatMessages(String functionName, String identifier) {
        return null;
    }

    @Override
    public Mono<List<ChatMessage>> getFeedbackChatMessages(String validatorName, String identifier) {
        return null;
    }

    @Override
    public Mono<Void> savePromptMessagesContext(String functionName, String identifier, ChatMessage message) {
        return null;
    }

    @Override
    public Mono<Void> saveFeedbackMessagesContext(String validatorName, String identifier, ChatMessage message) {
        return null;
    }
}
