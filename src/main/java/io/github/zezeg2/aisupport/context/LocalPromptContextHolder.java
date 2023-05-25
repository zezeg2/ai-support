package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPromptContextHolder implements PromptContextHolder {
    private static final Map<String, Prompt> registry = new ConcurrentHashMap<>();

    @Override
    public boolean containsPrompt(String function) {
        return registry.containsKey(function);
    }

    @Override
    public void addPromptToContext(String function, Prompt prompt) {
        registry.put(function, prompt);
    }

    @Override
    public Prompt getPrompt(String function) {
        return registry.get(function);
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessageContext(String function) {
        return getPrompt(function).getPromptMessageContext();
    }
}
