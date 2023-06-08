package io.github.zezeg2.aisupport.ai.function.prompt.refactor;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;
import java.util.Map;

public abstract class ResultValidator<S, M extends PromptManager<?>> implements Validatable<S> {
    protected final M promptManager;
    protected final ObjectMapper mapper;
    protected static final int MAX_ATTEMPTS = 3;

    public ResultValidator(M promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
    }

    public void init(String functionName) {
        List<ChatMessage> feedbackChatMessages = promptManager.getContext().getFeedbackChatMessages(functionName, this.getClass().getSimpleName(), promptManager.getIdentifier());
        Map<String, List<ChatMessage>> feedbackMessagesContext = promptManager.getContext().getFeedbackMessagesContext(functionName, this.getClass().getSimpleName());
        if (feedbackChatMessages.isEmpty()) {
            promptManager.addMessage(functionName, ROLE.SYSTEM, buildTemplate(functionName), feedbackMessagesContext);
        }
    }

    private String buildTemplate(String functionName) {
        String FEEDBACK_FRAME = """
                You are tasked with inspecting the provided Json and please provide feedback according to the given `Feedback Format`
                            
                Feedback Format:
                ```json
                %s
                ```
                                
                The inspection items are as follows.
                %s
                            
                            
                Do not include any other explanatory text in your response other than result
                """;
        return FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), addContents(functionName));
    }

    @Override
    public abstract S validate(String functionName) throws JsonProcessingException;

    protected abstract S getResponseContent(String functionName, String message, ContextType contextType);

    protected abstract S getLastPromptResponseContent(String functionName);

    protected abstract String addContents(String functionName);
}
