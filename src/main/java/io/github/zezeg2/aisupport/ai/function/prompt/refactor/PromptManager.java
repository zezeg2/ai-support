package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.exceptions.NotInitiatedContextException;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.servlet.ContextIdentifierProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public abstract class PromptManager<T> {
    protected final OpenAiService service;
    protected final PromptContextHolder context;
    protected final ContextIdentifierProvider identifierProvider;
    protected final ContextProperties contextProperties;

    public void addMessage(String functionName, ROLE role, String message) {
        String identifier = getIdentifier();
        Map<String, List<ChatMessage>> promptMessagesContext = context.getPromptMessagesContext(functionName);
        addMessageToContext(promptMessagesContext, functionName, identifier, role, message);
    }

    public void addMessage(String functionName, ROLE role, String message, Map<String, List<ChatMessage>> messageContext) {
        addMessageToContext(messageContext, functionName, getIdentifier(), role, message);
    }

    protected void addMessageToContext(Map<String, List<ChatMessage>> promptMessagesContext, String functionName, String identifier, ROLE role, String message) {
        if (!promptMessagesContext.containsKey(identifier)) {
            redisPersistenceSupport(functionName);
        }

        List<ChatMessage> chatMessages = promptMessagesContext.get(identifier);
        if (chatMessages.isEmpty() && !role.equals(ROLE.SYSTEM)) throw new NotInitiatedContextException();
        else chatMessages.add(new ChatMessage(role.getValue(), message));
    }

    protected void redisPersistenceSupport(String functionName) {
        if (functionName != null && contextProperties.getContext().equals(ContextProperties.CONTEXT.REDIS)) {
            Prompt prompt = context.get(functionName);
            context.save(functionName, prompt);
        }
    }

    public String getIdentifier() {
        return identifierProvider.getId();
    }

    public abstract T exchangePromptMessages(String functionName, AIModel model, boolean save);

    public abstract T exchangeFeedbackMessages(String functionName, String validatorName, AIModel model, boolean save);

    protected abstract T getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages);

    protected abstract T createChatCompletion(AIModel model, List<ChatMessage> messages);
}
