package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;

import java.util.List;
import java.util.Map;

public interface PromptContextHolder {

    boolean contains(String namespace);

    void savePrompt(String namespace, Prompt prompt);

    Prompt get(String namespace);

    Map<String, List<ChatMessage>> getPromptMessagesContext(String namespace);

    Map<String, List<ChatMessage>> getFeedbackMessagesContext(String namespace);

    List<ChatMessage> getPromptChatMessages(String namespace, String identifier);

    List<ChatMessage> getFeedbackChatMessages(String namespace, String identifier);

    void savePromptMessagesContext(String namespace, String identifier, ChatMessage message);

    void saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message);
}
