package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.FieldDesc;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;
import java.util.Map;

public abstract class ResultValidator implements Validatable {
    protected final PromptManager promptManager;
    protected final BuildFormatUtil formatUtil;

    public ResultValidator(PromptManager promptManager, BuildFormatUtil formatUtil) {
        this.promptManager = promptManager;
        this.formatUtil = formatUtil;
    }

    public void initFeedbackAssistantContext(String functionName){
        Prompt prompt = promptManager.getPrompt(functionName);
        Map<String, List<ChatMessage>> feedbackAssistantContext = prompt.getFeedbackAssistantContext();
        if (!feedbackAssistantContext.containsKey(promptManager.getIdentifier())) {
            promptManager.addMessage(functionName, ROLE.SYSTEM, ContextType.FEEDBACK, buildTemplate(functionName));
        }
    }

    public abstract String buildTemplate(String functionName);

    static class FeedbackResponse extends BaseSupportType{
        @FieldDesc("Determine whether the given result is flawless or not")
        boolean isValid;
        @FieldDesc("This is List of problems in the results")
        List<String> problems;
    }
}
