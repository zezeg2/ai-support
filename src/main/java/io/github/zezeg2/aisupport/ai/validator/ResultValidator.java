package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.exceptions.NotInitiatedContextException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ResultValidator implements Validatable {
    protected final PromptManager promptManager;
    protected final BuildFormatUtil formatUtil;
    protected final ObjectMapper mapper;
    private final Map<String, List<ChatMessage>> feedbackMessageContext;

    public ResultValidator(PromptManager promptManager, BuildFormatUtil formatUtil, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.formatUtil = formatUtil;
        this.mapper = mapper;
        this.feedbackMessageContext = new ConcurrentHashMap<>();
    }

    public void initFeedbackMessageContext(String functionName) {
        promptManager.initMessageContext(functionName, buildTemplate(functionName), feedbackMessageContext);
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
        return FEEDBACK_FRAME.formatted(formatUtil.getFormatString(FeedbackResponse.class), addContents(functionName));
    }

    protected abstract String addContents(String functionName);
}
