package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;

import java.util.List;

public abstract class DefaultResultValidator extends ResultValidator<String, DefaultPromptManager> {
    public DefaultResultValidator(DefaultPromptManager promptManager, ObjectMapper mapper) {
        super(promptManager, mapper);
    }

    @Override
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

    @Override
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

    @Override
    protected String getLastPromptResponseContent(String functionName) {
        List<ChatMessage> promptMessageList = promptManager.getContext().getPromptChatMessages(functionName, promptManager.getIdentifier());
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }
}
