package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.ROLE;

public class JsonProcessingExceptionValidator implements Validatable {
    protected final PromptManager promptManager;
    protected Exception exception;

    public JsonProcessingExceptionValidator(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    public String validate(String functionName) {
        System.out.println("ExceptionValidate");
        Prompt prompt = promptManager.getPrompt(functionName);
        String VALIDATE_EXCEPTION_TEMPLATE = """
                Fix a previous response that causes an following Exception
                ```
                Exception : %s
                Message : %s
                ```
                Remind Result Format and reply again
                            
                Result Format : %s
                """;
        promptManager.addMessage(functionName, ROLE.USER, VALIDATE_EXCEPTION_TEMPLATE.formatted(exception.getClass().getSimpleName(), exception.getMessage(), prompt.getResultFormat()));
        ChatMessage responseMessage = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.PROMPT, true);
        String content = responseMessage.getContent();
        System.out.println(content);
        return content;
    }
}
