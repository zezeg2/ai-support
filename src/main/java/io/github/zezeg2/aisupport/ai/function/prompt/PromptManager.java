package io.github.zezeg2.aisupport.ai.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.exceptions.NotInitiatedContextException;
import io.github.zezeg2.aisupport.context.ContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.PromptContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Getter
public class PromptManager {

    private final OpenAiService service;
    private final PromptContextHolder context;
    private final ContextIdentifierProvider identifierProvider;

    public Prompt getPrompt(String functionName) {
        return context.getPrompt(functionName);
    }

    public void initPromptContext(String functionName, Prompt prompt) {
        if (!context.containsPrompt(functionName)) {
            context.addPromptToContext(functionName, prompt);
        }
        Map<String, List<ChatMessage>> promptMessageContext = prompt.getPromptMessageContext();
        if (!promptMessageContext.containsKey(getIdentifier())) {
            addMessage(functionName, ROLE.SYSTEM, ContextType.PROMPT, prompt.toString());
        }
    }

    public void addMessage(String functionName, ROLE role, ContextType contextType, String message) {
        String identifier = getIdentifier();
        Map<String, List<ChatMessage>> messageContext;
        if (contextType.equals(ContextType.PROMPT))
            messageContext = context.getPrompt(functionName).getPromptMessageContext();
        else messageContext = context.getPrompt(functionName).getFeedbackAssistantContext();

        if (!messageContext.containsKey(identifier))
            messageContext.put(identifier, new CopyOnWriteArrayList<>());

        List<ChatMessage> chatMessages = messageContext.get(identifier);
        if (!chatMessages.isEmpty()) chatMessages.add(new ChatMessage(role.getValue(), message));
        else {
            if (role.equals(ROLE.SYSTEM)) chatMessages.add(new ChatMessage(role.getValue(), message));
            else throw new NotInitiatedContextException();
        }
    }

    public ChatCompletionResult exchangeMessages(String functionName, AIModel model, ContextType contextType, boolean save) {
        List<ChatMessage> contextMessages;
        if (contextType.equals(ContextType.PROMPT))
            contextMessages = context.getPrompt(functionName).getPromptMessageContext().get(getIdentifier());
        else contextMessages = context.getPrompt(functionName).getFeedbackAssistantContext().get(getIdentifier());
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        responseMessage.setContent(extractJsonFromMessage(responseMessage.getContent()));
        if (save) contextMessages.add(responseMessage);
        return response;
    }

    private String extractJsonFromMessage(String originalString) {
        int firstIndex = originalString.indexOf('{');
        int lastIndex = originalString.lastIndexOf('}') + 1;

        if (firstIndex != -1 && lastIndex != -1) {
            return originalString.substring(firstIndex, lastIndex);
        } else {
            return originalString;
        }
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

    public List<ChatMessage> getPromptMessageList(String functionName) {
        return context.getPromptMessageContext(functionName).get(getIdentifier());
    }

    public List<ChatMessage> getFeedbackAssistantMessageList(String functionName) {
        return context.getFeedbackAssistantMessageContext(functionName).get(getIdentifier());
    }
}
