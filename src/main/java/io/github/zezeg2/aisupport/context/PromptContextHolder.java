package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;

import java.util.List;
import java.util.Map;

public interface PromptContextHolder {

    boolean containsPrompt(String functionName);

    void addPromptToContext(String functionName, Prompt prompt);

    Prompt getPrompt(String functionName);

    Map<String, List<ChatMessage>> getPromptMessageContext(String functionName);

    Map<String, List<ChatMessage>> getFeedbackAssistantMessageContext(String functionName);
}
