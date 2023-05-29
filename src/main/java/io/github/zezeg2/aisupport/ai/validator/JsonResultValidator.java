package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;

@ValidateTarget(global = true)
public class JsonResultValidator extends ResultValidator {

    public JsonResultValidator(PromptManager promptManager, BuildFormatUtil formatUtil) {
        super(promptManager, formatUtil);
    }
    @Override
    public String addContents(String functionName) {
        String FEEDBACK_TEMPLATE = """  
                1. Ensure parseability: Check that the JSON string is valid and can be properly parsed as `Required Format`.
                  
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


        Prompt prompt = promptManager.getPrompt(functionName);
        return FEEDBACK_TEMPLATE.formatted(prompt.getResultFormat());
    }

    @Override
    public String validate(String functionName) throws Exception {
        initFeedbackAssistantContext(functionName);
        List<ChatMessage> promptMessageList = promptManager.getPromptMessageList(functionName);
        List<ChatMessage> feedbackAssistantMessageList = promptManager.getFeedbackAssistantMessageList(functionName);
        ChatMessage feedbackMessage;
        FeedbackResponse feedbackResult;
        int count = 0;
        while (true) {
            if (count > 2) {
                throw new RuntimeException("Maximum Validate count over");
            }
            System.out.println("try count : " + (count++) + "--------------------------------------------------------------");
            String lastPromptMessage = promptMessageList.get(promptMessageList.size() - 1).getContent();
            System.out.println(lastPromptMessage);
            promptManager.addMessage(functionName, ROLE.USER, ContextType.FEEDBACK, lastPromptMessage);
            feedbackMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.FEEDBACK, true).getChoices().get(0).getMessage();
            feedbackResult = mapper.readValue(feedbackMessage.getContent(), FeedbackResponse.class);

            String lastFeedbackMessage = feedbackAssistantMessageList.get(feedbackAssistantMessageList.size() - 1).getContent();
            System.out.println(feedbackResult);
            if (feedbackResult.isValid()) {
                return lastPromptMessage;
            }

            promptManager.addMessage(functionName, ROLE.USER, ContextType.PROMPT, lastFeedbackMessage);
            promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.PROMPT, true);
        }
    }
}
