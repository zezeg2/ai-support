package io.github.zezeg2.aisupport.ai.validator;

import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;

public abstract class ResultValidator implements Validatable {
    protected final PromptManager promptManager;

    public ResultValidator(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    public abstract void initFeedbackAssistantContext(String functionName);
}
