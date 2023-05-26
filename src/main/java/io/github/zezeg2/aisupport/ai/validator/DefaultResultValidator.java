package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ValidateTarget(global = true)
public class DefaultResultValidator extends ResultValidator {
    private final static String SYSTEM_TEMPLATE = """
            Make sure following JsonString strictly adheres to the given `Constraints` and `Result Format`. If the given jsonString is perfect, please respond with  just "true", otherwise please respond with feedback message to improve the jsonString. (response as String value)
                        
            Remind Constraints
            Constraints
            - Only respond with your `return` value. Do not include any other explanatory text in your response at all.
            %s
                        
            Result Format: %s
            """;

    public DefaultResultValidator(PromptManager promptManager) {
        super(promptManager);
    }

    @Override
    public void initFeedbackAssistantContext(String functionName) {
        Prompt prompt = promptManager.getPrompt(functionName);
        List<ChatMessage> feedbackAssistantMessageList = promptManager.getFeedbackAssistantMessageList(functionName);
        if (feedbackAssistantMessageList.isEmpty()) {
            promptManager.addMessage(functionName, ROLE.SYSTEM, ContextType.FEEDBACK, SYSTEM_TEMPLATE.formatted(prompt.getConstraints(), prompt.getResultFormat()));
        }
    }

    @Override
    public boolean isRequired(String functionName) {
//        initFeedbackAssistantContext(functionName);
//        Prompt prompt = promptManager.getPrompt(functionName);
//        promptManager.addMessage(functionName, ROLE.USER, CHECK_TEMPLATE.formatted(prompt.getConstraints()));
//        ChatMessage responseMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.FEEDBACK, true);
//        String content = parseBooleanFromString(responseMessage.getContent());
//        System.out.println(content);
//        return !Boolean.parseBoolean(content);
        return true;
    }

    @Override
    public String validate(String functionName) {
        initFeedbackAssistantContext(functionName);
        List<ChatMessage> promptMessageList = promptManager.getPromptMessageList(functionName);
        List<ChatMessage> feedbackAssistantMessageList = promptManager.getFeedbackAssistantMessageList(functionName);
        int count = 0;
        while (true) {
            if (count > 2){
                throw new RuntimeException("Maximum Validate count over");
            }
            System.out.println("try count : " +count++ + "--------------------------------------------------------------");
            String lastPromptMessage = promptMessageList.get(promptMessageList.size() - 1).getContent();
            System.out.println(lastPromptMessage);
            promptManager.addMessage(functionName, ROLE.USER, ContextType.FEEDBACK, lastPromptMessage);
            promptManager.exchangeMessages(functionName, ContextType.FEEDBACK, true);

            String lastFeedbackMessage = feedbackAssistantMessageList.get(feedbackAssistantMessageList.size() - 1).getContent();
            System.out.println(lastFeedbackMessage);
            if (Boolean.parseBoolean(lastFeedbackMessage.toLowerCase())) {
                return lastPromptMessage;
            }

            promptManager.addMessage(functionName, ROLE.USER, ContextType.PROMPT, lastFeedbackMessage + "Please respond again, incorporating the feedback");
            promptManager.exchangeMessages(functionName, ContextType.PROMPT, true);
        }
    }

    private String parseBooleanFromString(String input) {
        Pattern pattern = Pattern.compile("\\b(true|false)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input.toLowerCase());
        if (matcher.find()) return matcher.group();
        return "false";
    }
}
