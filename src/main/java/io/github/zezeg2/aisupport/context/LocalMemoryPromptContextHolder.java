package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalMemoryPromptContextHolder implements PromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<String, List<PromptMessages>> promptMessagesRegistry = new ConcurrentHashMap<>();
    private static final Map<String, List<FeedbackMessages>> feedbackMessagesRegistry = new ConcurrentHashMap<>();

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
    public PromptMessages getPromptChatMessages(String namespace, String identifier) {
        return promptMessagesRegistry.get(namespace).stream()
                .filter(promptMessages -> promptMessages.getIdentifier().equals(identifier)).findFirst()
                .orElse(PromptMessages.builder().identifier(identifier).content(new CopyOnWriteArrayList<>()).build());
    }

    @Override
    public FeedbackMessages getFeedbackChatMessages(String namespace, String identifier) {
        return feedbackMessagesRegistry.get(namespace).stream()
                .filter(feedbackMessages -> feedbackMessages.getIdentifier().equals(identifier)).findFirst()
                .orElse(FeedbackMessages.builder().identifier(identifier).content(new CopyOnWriteArrayList<>()).build());
    }

    @Override
    public void savePromptMessages(String namespace, String identifier, ChatMessage message) {
        if (!promptMessagesRegistry.containsKey(namespace)) {
            promptMessagesRegistry.put(namespace, new CopyOnWriteArrayList<>());
        }
        PromptMessages promptChatMessages = getPromptChatMessages(namespace, identifier);
        promptChatMessages.getContent().add(message);

        if (promptMessagesRegistry.get(namespace).stream()
                .filter(promptMessages -> promptMessages.getIdentifier().equals(identifier)).findFirst().isEmpty()) {
            promptMessagesRegistry.get(namespace).add(promptChatMessages);
        }
    }

    @Override
    public void saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        if (!feedbackMessagesRegistry.containsKey(namespace)) {
            feedbackMessagesRegistry.put(namespace, new CopyOnWriteArrayList<>());
        }
        FeedbackMessages feedbackMessages = getFeedbackChatMessages(namespace, identifier);
        feedbackMessages.getContent().add(message);

        if (feedbackMessagesRegistry.get(namespace).stream()
                .filter(promptMessages -> promptMessages.getIdentifier().equals(identifier)).findFirst().isEmpty()) {
            feedbackMessagesRegistry.get(namespace).add(feedbackMessages);
        }
    }
}
