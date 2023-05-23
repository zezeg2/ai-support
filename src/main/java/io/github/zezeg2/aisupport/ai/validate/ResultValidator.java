package io.github.zezeg2.aisupport.ai.validate;

import com.theokanning.openai.service.OpenAiService;

public abstract class ResultValidator implements Validator {
    protected final OpenAiService service;
    protected final String promptKey;

    public ResultValidator(OpenAiService service, String promptKey) {
        this.service = service;
        this.promptKey = promptKey;
    }
}
