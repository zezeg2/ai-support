package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromptContext {
    private static final Map<String, Prompt> registry = new ConcurrentHashMap<>() {
    };

    public static boolean containsPrompt(String function) {
        return registry.containsKey(function);
    }

    public static void addPromptToContext(String function, Prompt prompt) {
        registry.put(function, prompt);
    }

    public static Prompt getPrompt(String function) {
        return registry.get(function);
    }

    public static Map<String, List<ChatMessage>> getPromptMessageContext(String function) {
        return getPrompt(function).getPromptMessageContext();
    }
}
