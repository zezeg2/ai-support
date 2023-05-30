package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import org.springframework.stereotype.Component;

import java.util.List;

@ValidateTarget(global = true)
@Component
public class JsonResultValidator extends ResultValidator {

    private static final int MAX_ATTEMPTS = 3;
    private static final String FEEDBACK_TEMPLATE = """
            1. Ensure Parse-ability: Check that the JSON string is valid and can be properly parsed as `Required Format`.
            2. Verify schema compliance: The JSON string should comply with a given schema, omitting any objects or fields mentioned in the schema but not found in the JSON.
            3. Escape double quotes: Within JSON string values, double quotes should be correctly escaped to ensure JSON validity.
            4. Handle incorrectly escaped characters: Check for and handle any incorrectly escaped characters within the JSON string.
            5. Replace None or NaN values: Any 'None' or 'NaN' values in the JSON string should be replaced with 'null' to facilitate correct parsing.
            6. Parse the JSON: Parse the cleaned, schema-compliant JSON.
                    
            Required Format:
            ```json
            %s
            ```
            """;

    public JsonResultValidator(PromptManager promptManager, BuildFormatUtil formatUtil) {
        super(promptManager, formatUtil);
    }

    @Override
    public String addContents(String functionName) {
        Prompt prompt = promptManager.getPrompt(functionName);
        return String.format(FEEDBACK_TEMPLATE, prompt.getResultFormat());
    }

    @Override
    public String validate(String functionName) throws JsonProcessingException {
        initFeedbackAssistantContext(functionName);
        String lastPromptMessage;

        for (int count = 1; count <= MAX_ATTEMPTS; count++) {
            System.out.println("Try Count : " + count + "--------------------------------------------------------------");
            lastPromptMessage = getLastPromptResponseContent(functionName);
            System.out.println(lastPromptMessage);

            String feedbackContent = getFeedbackResponseContent(functionName, lastPromptMessage);
            FeedbackResponse feedbackResult = mapper.readValue(feedbackContent, FeedbackResponse.class);

            System.out.println(feedbackResult);
            if (feedbackResult.isValid()) {
                return lastPromptMessage;
            }
        }

        throw new RuntimeException("Maximum Validate count over");
    }

    private String getLastPromptResponseContent(String functionName) {
        List<ChatMessage> promptMessageList = promptManager.getPromptMessageList(functionName);
        return promptMessageList.get(promptMessageList.size() - 1).getContent();
    }

    private String getFeedbackResponseContent(String functionName, String lastPromptMessage) {
        promptManager.addMessage(functionName, ROLE.USER, ContextType.FEEDBACK, lastPromptMessage);
        ChatMessage feedbackMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.FEEDBACK, true).getChoices().get(0).getMessage();
        return feedbackMessage.getContent();
    }
}
