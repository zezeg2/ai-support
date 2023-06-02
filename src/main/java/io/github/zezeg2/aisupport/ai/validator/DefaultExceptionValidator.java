package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.ROLE;

import java.util.List;

public class DefaultExceptionValidator extends ExceptionValidator {

    public DefaultExceptionValidator(PromptManager promptManager, ObjectMapper mapper) {
        super(promptManager, mapper);
    }

    @Override
    public String validate(String functionName, Exception exception) throws JsonProcessingException {
        System.out.println(exception.getClass().getSimpleName() + "Occurred");
        feedbackResponse.setValid(false);
        feedbackResponse.setFeedbacks(List.of(exception.getMessage()));

        promptManager.addMessage(functionName, ROLE.USER, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(feedbackResponse));
        ChatCompletionResult response = promptManager.exchangeMessages(functionName, GPT3Model.GPT_3_5_TURBO, true);
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);
        return content;
    }
}
