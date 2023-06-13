package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalMemoryPromptContextHolder implements PromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<ChatMessage>>> promptMessagesRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<ChatMessage>>> feedbackMessagesRegistry = new ConcurrentHashMap<>();

    @Override
    public boolean contains(String namespace) {
        return promptRegistry.containsKey(namespace);
    }

    @Override
    public void savePrompt(String namespace, Prompt prompt) {
        promptRegistry.put(namespace, prompt);
    }

    @Override
    public Prompt get(String namespace) {
        return promptRegistry.get(namespace);
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessagesContext(String namespace) {
        return promptMessagesRegistry.get(namespace);
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackMessagesContext(String namespace) {
        return feedbackMessagesRegistry.get(namespace);
    }

    @Override
    public List<ChatMessage> getPromptChatMessages(String namespace, String identifier) {
        return promptMessagesRegistry.get(namespace).get(identifier);
    }

    @Override
    public List<ChatMessage> getFeedbackChatMessages(String namespace, String identifier) {
        return feedbackMessagesRegistry.get(namespace).get(identifier);
    }

    @Override
    public void savePromptMessagesContext(String namespace, String identifier, ChatMessage message) {
        Map<String, List<ChatMessage>> identifierMessages = promptMessagesRegistry.getOrDefault(namespace, new ConcurrentHashMap<>());
        List<ChatMessage> messages = identifierMessages.getOrDefault(identifier, new ArrayList<>());
        messages.add(message);
        identifierMessages.put(identifier, messages);
        promptMessagesRegistry.put(namespace, identifierMessages);
    }

    @Override
    public void saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message) {
        Map<String, List<ChatMessage>> identifierMessages = feedbackMessagesRegistry.getOrDefault(namespace, new ConcurrentHashMap<>());
        List<ChatMessage> messages = identifierMessages.getOrDefault(identifier, new ArrayList<>());
        messages.add(message);
        identifierMessages.put(identifier, messages);
        feedbackMessagesRegistry.put(namespace, identifierMessages);
    }
}
