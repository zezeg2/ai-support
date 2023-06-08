package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.ContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.PromptContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class DefaultPromptManager {

    protected final OpenAiService service;
    protected final PromptContextHolder context;
    protected final ContextIdentifierProvider identifierProvider;
    protected final ContextProperties contextProperties;

    public void addMessage(String functionName, ROLE role, String message, ContextType contextType) {
        addMessageToContext(functionName, role, message, contextType);
    }

    protected void addMessageToContext(String functionName, ROLE role, String message, ContextType contextType) {
        String identifier = getIdentifier();
        switch (contextType) {
            case PROMPT ->
                    context.savePromptMessagesContext(functionName, identifier, new ChatMessage(role.getValue(), message));
            case FEEDBACK ->
                    context.saveFeedbackMessagesContext(functionName, identifier, new ChatMessage(role.getValue(), message));
        }
    }

    public String getIdentifier() {
        return identifierProvider.getId();
    }

    public ChatCompletionResult exchangePromptMessages(String functionName, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = context.getPromptChatMessages(functionName, getIdentifier());
        return getChatCompletionResult(functionName, model, save, contextMessages, ContextType.PROMPT);
    }

    public ChatCompletionResult exchangeFeedbackMessages(String validatorName, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = context.getFeedbackChatMessages(validatorName, getIdentifier());
        return getChatCompletionResult(validatorName, model, save, contextMessages, ContextType.FEEDBACK);
    }

    protected ChatCompletionResult getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
        if (save) contextMessages.add(responseMessage);
        return response;
    }

    protected ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build());
    }
}
