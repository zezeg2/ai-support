package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;

import java.util.List;
import java.util.Map;

public interface PromptContext {

    boolean containsPrompt(String function);

    void addPromptToContext(String function, Prompt prompt);

    Prompt getPrompt(String function);

    Map<String, List<ChatMessage>> getPromptMessageContext(String function);
}
