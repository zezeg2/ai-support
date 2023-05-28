package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;
import java.util.Map;

public abstract class ResultValidator implements Validatable {
    protected final PromptManager promptManager;
    protected final BuildFormatUtil formatUtil;
    protected static ObjectMapper mapper = new ObjectMapper();

    public ResultValidator(PromptManager promptManager, BuildFormatUtil formatUtil) {
        this.promptManager = promptManager;
        this.formatUtil = formatUtil;
    }

    public void initFeedbackAssistantContext(String functionName) throws Exception {
        Prompt prompt = promptManager.getPrompt(functionName);
        Map<String, List<ChatMessage>> feedbackAssistantContext = prompt.getFeedbackAssistantContext();
        if (!feedbackAssistantContext.containsKey(promptManager.getIdentifier())) {
            promptManager.addMessage(functionName, ROLE.SYSTEM, ContextType.FEEDBACK, buildTemplate(functionName));
        }
    }

    private String buildTemplate(String functionName) throws Exception {
        String FEEDBACK_FRAME = """
                You are tasked with inspecting the provided Json and please provide feedback according to the given `Feedback Format`
                            
                %s
                            
                Feedback Format:
                ```json
                %s
                ```
                            
                Do not include any other explanatory text in your response other than result
                """;
        return FEEDBACK_FRAME.formatted(addContents(functionName), formatUtil.getFormatString(FeedbackResponse.class));
    }

    protected abstract String addContents(String functionName);
}
