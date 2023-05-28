package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ValidateTarget(global = true)
public class DefaultResultValidator extends ResultValidator {

    public DefaultResultValidator(PromptManager promptManager, BuildFormatUtil formatUtil) {
        super(promptManager, formatUtil);
    }
    @Override
    public String buildTemplate(String functionName) {
        String FEEDBACK_TEMPLATE = """
            You are tasked with inspecting the provided Json and supplying feedback.
            Firstly, verify that the supplied Json is in strict accordance with the `Required Format`.
            Secondly, inspect the Json content for full compliance with each item in the given `Constraints`.
            If the inspection results are flawless, respond with the term "true".
            If there are any issues identified from the inspection, provide the results as feedback.
            The response should be limited to the inspection results, without additional explanation.
                        
            Constraints
            %s
                        
            Required Format:
            ```json
            %s
            ```
            """;
        Prompt prompt = promptManager.getPrompt(functionName);
        return FEEDBACK_TEMPLATE.formatted(prompt.getConstraints(), prompt.getResultFormat());
    }

    @Override
    public String validate(String functionName) {
        initFeedbackAssistantContext(functionName);
        List<ChatMessage> promptMessageList = promptManager.getPromptMessageList(functionName);
        List<ChatMessage> feedbackAssistantMessageList = promptManager.getFeedbackAssistantMessageList(functionName);
        int count = 0;
        while (true) {
            if (count > 2) {
                throw new RuntimeException("Maximum Validate count over");
            }
            System.out.println("try count : " + (count++) + "--------------------------------------------------------------");
            String lastPromptMessage = promptMessageList.get(promptMessageList.size() - 1).getContent();
            System.out.println(lastPromptMessage);
            promptManager.addMessage(functionName, ROLE.USER, ContextType.FEEDBACK, lastPromptMessage);
            promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.FEEDBACK, true);

            String lastFeedbackMessage = feedbackAssistantMessageList.get(feedbackAssistantMessageList.size() - 1).getContent();
            System.out.println(lastFeedbackMessage);
            if (Boolean.parseBoolean(parseBooleanFromString(lastFeedbackMessage))) {
                return lastPromptMessage;
            }

            promptManager.addMessage(functionName, ROLE.USER, ContextType.PROMPT, lastFeedbackMessage + "Respond again, incorporating the feedback");
            promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.PROMPT, true);
        }
    }

    private String parseBooleanFromString(String input) {
        Pattern pattern = Pattern.compile("\\b(true|false)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input.toLowerCase());
        if (matcher.find()) return matcher.group();
        return "false";
    }
}
