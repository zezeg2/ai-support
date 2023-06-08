package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;
import java.util.Map;

public interface PromptContextHolder {

    boolean contains(String functionName);

    void save(String functionName, Prompt prompt);

    Prompt get(String functionName);

    Map<String, List<ChatMessage>> getPromptMessagesContext(String functionName);

    Map<String, List<ChatMessage>> getFeedbackMessagesContext(String functionName, String validatorName);

    List<ChatMessage> getPromptChatMessages(String functionName, String identifier);

    List<ChatMessage> getFeedbackChatMessages(String functionName, String validatorName, String identifier);
}
