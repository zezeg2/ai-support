package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ResultValidator implements Validatable {
    protected final PromptManager promptManager;
    protected final ObjectMapper mapper;
    protected final Map<String, List<ChatMessage>> feedbackMessageContext;
    private static final int MAX_ATTEMPTS = 3;

    public ResultValidator(PromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
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
        return FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), addContents(functionName));
    }

    @Override
    public String validate(String functionName) throws JsonProcessingException {
        initFeedbackMessageContext(functionName);
        String lastPromptMessage;
        String feedbackContent;
        lastPromptMessage = getLastPromptResponseContent(functionName);

        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + "--------------------------------------------------------------");
            System.out.println(lastPromptMessage);

            feedbackContent = getResponseContent(functionName, lastPromptMessage, ContextType.FEEDBACK);
            FeedbackResponse feedbackResult = mapper.readValue(feedbackContent, FeedbackResponse.class);

            System.out.println(feedbackResult);
            if (feedbackResult.isValid()) {
                return lastPromptMessage;
            }

            lastPromptMessage = getResponseContent(functionName, lastPromptMessage, ContextType.PROMPT);
        }

        throw new RuntimeException("Maximum Validate count over");
    }

    private String getResponseContent(String functionName, String message, ContextType contextType) {
        Map<String, List<ChatMessage>> messageContext = switch (contextType) {
            case PROMPT -> promptManager.getPrompt(functionName).getPromptMessageContext();
            case FEEDBACK -> feedbackMessageContext;
        };
        promptManager.addMessage(functionName, ROLE.USER, message, messageContext);
        ChatMessage responseMessage = promptManager.exchangeMessages(functionName, messageContext, GPT3Model.GPT_3_5_TURBO, true).getChoices().get(0).getMessage();
        return responseMessage.getContent();
    }

    private String getLastPromptResponseContent(String functionName) {
        List<ChatMessage> promptMessageList = promptManager.getPrompt(functionName).getPromptMessageContext().get(promptManager.getIdentifier());
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    protected abstract String addContents(String functionName);
}
