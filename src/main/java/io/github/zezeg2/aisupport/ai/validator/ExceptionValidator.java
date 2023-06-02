package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;

public abstract class ExceptionValidator {
    protected final PromptManager promptManager;

    protected final ObjectMapper mapper;
    protected final FeedbackResponse feedbackResponse = new FeedbackResponse();

    public ExceptionValidator(PromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
    }

    public abstract String validate(String functionName, Exception exception) throws JsonProcessingException;
}
