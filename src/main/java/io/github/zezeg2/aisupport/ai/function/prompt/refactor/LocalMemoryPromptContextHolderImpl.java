package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalMemoryPromptContextHolderImpl implements PromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<ChatMessage>>> promptMessagesRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<ChatMessage>>> feedbackMessagesRegistry = new ConcurrentHashMap<>();

    @Override
    public boolean contains(String functionName) {
        return promptRegistry.containsKey(functionName);
    }

    @Override
    public void savePrompt(String functionName, Prompt prompt) {
        promptRegistry.put(functionName, prompt);
    }

    @Override
    public Prompt get(String functionName) {
        return promptRegistry.get(functionName);
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessagesContext(String functionName) {
        return promptMessagesRegistry.get(functionName);
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackMessagesContext(String validatorName) {
        return feedbackMessagesRegistry.get(validatorName);
    }

    @Override
    public List<ChatMessage> getPromptChatMessages(String functionName, String identifier) {
        return promptMessagesRegistry.get(functionName).get(identifier);
    }

    @Override
    public List<ChatMessage> getFeedbackChatMessages(String validatorName, String identifier) {
        return feedbackMessagesRegistry.get(validatorName).get(identifier);
    }

    @Override
    public void savePromptMessagesContext(String functionName, String identifier, ChatMessage message) {
        Map<String, List<ChatMessage>> identifierMessages = promptMessagesRegistry.getOrDefault(functionName, new ConcurrentHashMap<>());
        List<ChatMessage> messages = identifierMessages.getOrDefault(identifier, new ArrayList<>());
        messages.add(message);
        identifierMessages.put(identifier, messages);
        promptMessagesRegistry.put(functionName, identifierMessages);
    }

    @Override
    public void saveFeedbackMessagesContext(String validatorName, String identifier, ChatMessage message) {
        Map<String, List<ChatMessage>> identifierMessages = feedbackMessagesRegistry.getOrDefault(validatorName, new ConcurrentHashMap<>());
        List<ChatMessage> messages = identifierMessages.getOrDefault(identifier, new ArrayList<>());
        messages.add(message);
        identifierMessages.put(identifier, messages);
        feedbackMessagesRegistry.put(validatorName, identifierMessages);
    }
}
