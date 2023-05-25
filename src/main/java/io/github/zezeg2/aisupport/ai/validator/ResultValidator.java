package io.github.zezeg2.aisupport.ai.validator;

import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;

public abstract class ResultValidator implements Validatable {
    public ResultValidator(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    protected final PromptManager promptManager;
}
