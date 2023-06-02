package io.github.zezeg2.aisupport.context;

import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPromptContextHolder implements PromptContextHolder {
    private static final Map<String, Prompt> registry = new ConcurrentHashMap<>();

    @Override
    public boolean containsPrompt(String functionName) {
        return registry.containsKey(functionName);
    }

    @Override
    public void savePromptToContext(String functionName, Prompt prompt) {
        registry.put(functionName, prompt);
    }

    @Override
    public Prompt getPrompt(String functionName) {
        return registry.get(functionName);
    }
}
