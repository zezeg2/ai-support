package io.github.zezeg2.aisupport.ai.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.exceptions.NotInitiatedContextException;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.servlet.ContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.servlet.PromptContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Getter
public class PromptManager {

    private final OpenAiService service;
    private final PromptContextHolder context;
    private final ContextIdentifierProvider identifierProvider;
    private final ContextProperties contextProperties;

    public Prompt getPrompt(String functionName) {
        return context.getPrompt(functionName);
    }

    @Transactional
    public void initPromptContext(String functionName, Prompt prompt) {
        if (!context.containsPrompt(functionName)) {
            context.savePromptToContext(functionName, prompt);
        }
        Map<String, List<ChatMessage>> promptMessageContext = prompt.getPromptMessageContext();
        if (!promptMessageContext.containsKey(getIdentifier())) {
            addMessage(functionName, ROLE.SYSTEM, prompt.toString());
        }
    }

    @Transactional
    public void initPromptContext(String functionName) {
        Prompt prompt = context.getPrompt(functionName);
        initPromptContext(functionName, prompt);
    }

    @Transactional
    public void initMessageContext(String functionName, String systemMessage, Map<String, List<ChatMessage>> messageContext) {
        if (!messageContext.containsKey(getIdentifier())) {
            addMessage(functionName, ROLE.SYSTEM, systemMessage, messageContext);
        }
    }

    @Transactional
    public void addMessage(String functionName, ROLE role, String message) {
        String identifier = getIdentifier();
        Prompt prompt = context.getPrompt(functionName);
        Map<String, List<ChatMessage>> messageContext = prompt.getPromptMessageContext();

        addMessageToContext(messageContext, functionName, identifier, role, message);
    }

    public void addMessage(String functionName, ROLE role, String message, Map<String, List<ChatMessage>> messageContext) {
        String identifier = getIdentifier();
        addMessageToContext(messageContext, functionName, identifier, role, message);
    }

    private void addMessageToContext(Map<String, List<ChatMessage>> messageContext, String functionName, String identifier, ROLE role, String message) {
        if (!messageContext.containsKey(identifier)) {
            messageContext.put(identifier, new CopyOnWriteArrayList<>());
            redisPersistenceSupport(functionName);
        }

        List<ChatMessage> chatMessages = messageContext.get(identifier);
        if (chatMessages.isEmpty() && !role.equals(ROLE.SYSTEM)) {
            throw new NotInitiatedContextException();
        } else {
            chatMessages.add(new ChatMessage(role.getValue(), message));
        }
    }

    private void redisPersistenceSupport(String functionName) {
        if (functionName != null && contextProperties.getContext().equals(ContextProperties.CONTEXT.REDIS)) {
            Prompt prompt = context.getPrompt(functionName);
            context.savePromptToContext(functionName, prompt);
        }
    }

    public ChatCompletionResult exchangeMessages(String functionName, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = context.getPrompt(functionName).getPromptMessageContext().get(getIdentifier());
        return getChatCompletionResult(functionName, model, save, contextMessages);
    }

    public ChatCompletionResult exchangeMessages(String functionName, Map<String, List<ChatMessage>> messageContext, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = messageContext.get(getIdentifier());
        return getChatCompletionResult(functionName, model, save, contextMessages);
    }

    private ChatCompletionResult getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages) {
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
        if (save) {
            contextMessages.add(responseMessage);
            redisPersistenceSupport(functionName);
        }
        return response;
    }

    public ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build());
    }

    public String getIdentifier() {
        return identifierProvider.getId();
    }
}
