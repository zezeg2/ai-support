package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.PromptContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class PromptManager {

    protected final OpenAiService service;
    protected final PromptContextHolder context;
    protected final ContextProperties contextProperties;

    public void addMessage(String namespace, String identifier, ROLE role, String message, ContextType contextType) {
        addMessageToContext(namespace, identifier, role, message, contextType);
    }

    protected void addMessageToContext(String namespace, String identifier, ROLE role, String message, ContextType contextType) {
        switch (contextType) {
            case PROMPT -> context.savePromptMessages(namespace, identifier, new ChatMessage(role.getValue(), message));
            case FEEDBACK ->
                    context.saveFeedbackMessages(namespace, identifier, new ChatMessage(role.getValue(), message));
        }
    }

    public ChatCompletionResult exchangePromptMessages(String namespace, String identifier, AIModel model, double topP, boolean save) {
        List<ChatMessage> contextMessages = context.getPromptChatMessages(namespace, identifier).getContent();
        return getChatCompletionResult(namespace, identifier, model, topP, save, contextMessages, ContextType.PROMPT);
    }

    public ChatCompletionResult exchangeFeedbackMessages(String namespace, String identifier, AIModel model, double topP, boolean save) {
        List<ChatMessage> contextMessages = context.getFeedbackChatMessages(namespace, identifier).getContent();
        return getChatCompletionResult(namespace, identifier, model, topP, save, contextMessages, ContextType.FEEDBACK);
    }

    protected ChatCompletionResult getChatCompletionResult(String namespace, String identifier, AIModel model, double topP, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        ChatCompletionResult response = createChatCompletion(model, contextMessages, topP);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
        if (save) {
            switch (contextType) {
                case PROMPT -> context.savePromptMessages(namespace, identifier, responseMessage);
                case FEEDBACK -> context.saveFeedbackMessages(namespace, identifier, responseMessage);
            }
            contextMessages.add(responseMessage);
        }
        return response;
    }

    protected ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages, double topP) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .topP(topP)
                .build());
    }
}
