package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;

import java.util.List;

public abstract class DefaultResultValidator {
    protected static final int MAX_ATTEMPTS = 3;
    protected final DefaultPromptManager promptManager;
    protected final ObjectMapper mapper;

    public DefaultResultValidator(DefaultPromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
    }


    protected String getName(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName().toLowerCase()));
    }

    protected void init(String functionName) {
        List<ChatMessage> feedbackChatMessages = promptManager.getContext().getFeedbackChatMessages(getName(functionName), promptManager.getIdentifier());
        if (feedbackChatMessages.isEmpty()) {
            promptManager.addMessage(functionName, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
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

    public String validate(String functionName) throws JsonProcessingException {

        init(functionName);
        String lastPromptMessage;
        String feedbackContent;
        lastPromptMessage = getLastPromptResponseContent(functionName);

        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + "--------------------------------------------------------------");
            System.out.println(lastPromptMessage);

            feedbackContent = getResponseContent(getName(functionName), lastPromptMessage, ContextType.FEEDBACK);
            FeedbackResponse feedbackResult = mapper.readValue(feedbackContent, FeedbackResponse.class);

            System.out.println(feedbackResult);
            if (feedbackResult.isValid()) {
                return lastPromptMessage;
            }

            lastPromptMessage = getResponseContent(functionName, feedbackContent, ContextType.PROMPT);
        }

        throw new RuntimeException("Maximum Validate count over");
    }

    protected String getResponseContent(String functionName, String message, ContextType contextType) {
        promptManager.addMessage(functionName, ROLE.USER, message, contextType);
        ChatMessage responseMessage = switch (contextType) {
            case PROMPT ->
                    promptManager.exchangePromptMessages(functionName, GPT3Model.GPT_3_5_TURBO, true).getChoices().get(0).getMessage();
            case FEEDBACK ->
                    promptManager.exchangeFeedbackMessages(getName(functionName), GPT3Model.GPT_3_5_TURBO, true).getChoices().get(0).getMessage();
        };
        return responseMessage.getContent();
    }

    protected String getLastPromptResponseContent(String functionName) {
        List<ChatMessage> promptMessageList = promptManager.getContext().getPromptChatMessages(functionName, promptManager.getIdentifier());
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    protected abstract String addContents(String functionName);
}
