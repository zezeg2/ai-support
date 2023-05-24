package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.context.PromptContext;

public abstract class ResultValidator implements Validatable {
    protected final OpenAiService service;
    protected final PromptContext context;

    public ResultValidator(OpenAiService service, PromptContext context) {
        this.service = service;
        this.context = context;
    }
}
