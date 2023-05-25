package io.github.zezeg2.aisupport.ai.validator;

import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import lombok.Data;

@Data
public abstract class ExceptionValidator implements Validatable {
    protected final PromptManager promptManager;
    protected Exception exception;

    public ExceptionValidator(PromptManager promptManager) {
        this.promptManager = promptManager;
    }
}
