package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import lombok.Setter;

import java.util.List;

public class DefaultExceptionValidator implements Validatable {
    protected final PromptManager promptManager;
    @Setter
    protected Exception exception;
    protected static ObjectMapper mapper = new ObjectMapper();
    protected final FeedbackResponse feedbackResponse = new FeedbackResponse();

    public DefaultExceptionValidator(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    public String validate(String functionName) throws JsonProcessingException {
        System.out.println(exception.getClass().getSimpleName() + "Occurred");
        feedbackResponse.setValid(false);
        feedbackResponse.setFeedbacks(List.of(exception.getMessage()));

        promptManager.addMessage(functionName, ROLE.USER, ContextType.PROMPT, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(feedbackResponse));
        ChatCompletionResult response = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, ContextType.PROMPT, true);
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return content;
    }
}
