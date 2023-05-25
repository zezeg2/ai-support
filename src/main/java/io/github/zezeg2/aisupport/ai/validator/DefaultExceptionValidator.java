package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.ROLE;

public class DefaultExceptionValidator extends ExceptionValidator {
    private final String VALIDATE_EXCEPTION_TEMPLATE = """
            Fix a previous response that causes an following Exception
            ```
            Exception : %s
            Message : %s
            ```
            Remind Result Format and reply again
            
            Result Format : %s
            """;

    public DefaultExceptionValidator(PromptManager promptManager) {
        super(promptManager);
    }

    @Override
    public boolean isRequired(String functionName, String target) {
        return true;
    }

    @Override
    public String validate(String functionName, String target) {
        Prompt prompt = promptManager.getPrompt(functionName);
        promptManager.addMessage(functionName, ROLE.USER, VALIDATE_EXCEPTION_TEMPLATE.formatted(exception.getClass().getSimpleName(), exception.getMessage(), prompt.getResultFormat()));
        ChatMessage responseMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, true);
        return responseMessage.getContent();
    }
}
