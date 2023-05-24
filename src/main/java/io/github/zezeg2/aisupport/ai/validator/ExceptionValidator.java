package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.context.PromptContext;

public abstract class ExceptionValidator implements Validatable {
    protected final OpenAiService service;
    protected final PromptContext context;
    protected final Exception exception;

    public ExceptionValidator(OpenAiService service, PromptContext context, Exception exception) {
        this.service = service;
        this.context = context;
        this.exception = exception;
    }
}
