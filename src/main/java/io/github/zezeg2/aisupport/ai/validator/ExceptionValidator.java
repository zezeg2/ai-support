package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;

public abstract class ExceptionValidator {
    protected final PromptManager promptManager;

    protected static ObjectMapper mapper;
    protected final FeedbackResponse feedbackResponse = new FeedbackResponse();

    public ExceptionValidator(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    public abstract String validate(String functionName, Exception exception) throws JsonProcessingException;
}
