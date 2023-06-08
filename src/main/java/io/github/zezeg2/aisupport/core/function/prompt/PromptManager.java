package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
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
public abstract class PromptManager<T> {
    protected final OpenAiService service;
    protected final PromptContextHolder context;
    protected final ContextIdentifierProvider identifierProvider;
    protected final ContextProperties contextProperties;

    public void addMessage(String functionName, ROLE role, String message, ContextType contextType) {
        addMessageToContext(functionName, getIdentifier(), role, message, contextType);
    }

    protected void addMessageToContext(String functionName, String identifier, ROLE role, String message, ContextType contextType) {
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

    public abstract T exchangePromptMessages(String functionName, AIModel model, boolean save);

    public abstract T exchangeFeedbackMessages(String validatorName, AIModel model, boolean save);

    protected abstract T getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages, ContextType contextType);

    protected abstract T createChatCompletion(AIModel model, List<ChatMessage> messages);
}
