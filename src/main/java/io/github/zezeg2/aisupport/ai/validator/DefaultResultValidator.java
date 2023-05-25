package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.ROLE;

public class DefaultResultValidator extends ResultValidator {
    private final String CHECK_TEMPLATE = """
            Make sure your previous response strictly adheres to the given `Constraints`. and just answer whether your judgment is `true` or `false`(response will parsed to Java `boolean`). 
            Do not include any other explanatory text in your response at all
            
            Remind Constraints
            Constraints
            - Only respond with your `return` value. Do not include any other explanatory text in your response at all.
            %s
            """;

    private final String AMEND_TEMPLATE = """
            Please revise your response to comply with the constraints.
            """;
    public DefaultResultValidator(PromptManager promptManager) {
        super(promptManager);
    }

    @Override
    public boolean isRequired(String functionName, String target) {
        Prompt prompt = promptManager.getPrompt(functionName);
        promptManager.addMessage(functionName, ROLE.USER, CHECK_TEMPLATE.formatted(prompt.getConstraints()));
        ChatMessage responseMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, true);
        String content = responseMessage.getContent();
        System.out.println(content);
        return !Boolean.parseBoolean(content);
    }

    @Override
    public String validate(String functionName, String target) {
        promptManager.addMessage(functionName, ROLE.USER, AMEND_TEMPLATE.formatted());
        ChatMessage responseMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, true);
        String content = responseMessage.getContent();
        System.out.println(content);
        return content;
    }
}
