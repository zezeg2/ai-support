package io.github.zezeg2.aisupport.context;

import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;

public interface PromptContextHolder {

    boolean containsPrompt(String functionName);

    void savePromptToContext(String functionName, Prompt prompt);

    Prompt getPrompt(String functionName);
}
