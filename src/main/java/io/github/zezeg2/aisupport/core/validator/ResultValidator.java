package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
public abstract class ResultValidator {
    protected static final int MAX_ATTEMPTS = 3;
    protected final PromptManager promptManager;
    protected final ObjectMapper mapper;

    public ResultValidator(PromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
    }


    protected String getNamespace(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName()));
    }

    protected void init(String functionName, String identifier) {
        FeedbackMessages feedbackChatMessages = promptManager.getContext().getFeedbackChatMessages(getNamespace(functionName), identifier);
        if (feedbackChatMessages.getContent().isEmpty()) {
            promptManager.addMessage(getNamespace(functionName), identifier, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
        }
    }

    private String buildTemplate(String functionName) {
        return TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), addContents(functionName));
    }

    public String validate(String identifier, String functionName) throws JsonProcessingException {
        init(functionName, identifier);
        String lastPromptMessage;
        String feedbackContent;
        lastPromptMessage = getLastPromptResponseContent(functionName, identifier);

        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + "--------------------------------------------------------------");
            System.out.println(lastPromptMessage);

            feedbackContent = exchangeMessages(getNamespace(functionName), identifier, lastPromptMessage, ContextType.FEEDBACK);
            FeedbackResponse feedbackResult = mapper.readValue(feedbackContent, FeedbackResponse.class);

            System.out.println(feedbackResult);
            if (feedbackResult.isValid()) {
                return lastPromptMessage;
            }

            lastPromptMessage = exchangeMessages(functionName, identifier, feedbackContent, ContextType.PROMPT);
        }

        throw new RuntimeException("Maximum Validate count over");
    }

    protected String exchangeMessages(String functionName, String identifier, String message, ContextType contextType) {
        promptManager.addMessage(functionName, identifier, ROLE.USER, message, contextType);
        ChatMessage responseMessage = switch (contextType) {
            case PROMPT ->
                    promptManager.exchangePromptMessages(functionName, identifier, GPT3Model.GPT_3_5_TURBO, true).getChoices().get(0).getMessage();
            case FEEDBACK ->
                    promptManager.exchangeFeedbackMessages(functionName, identifier, GPT3Model.GPT_3_5_TURBO, true).getChoices().get(0).getMessage();
        };
        return responseMessage.getContent();
    }

    protected String getLastPromptResponseContent(String functionName, String identifier) {
        List<ChatMessage> promptMessageList = promptManager.getContext().getPromptChatMessages(functionName, identifier).getContent();
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    protected abstract String addContents(String functionName);

    protected Prompt getPrompt(String functionName){
        return promptManager.getContext().get(functionName);
    }
}
