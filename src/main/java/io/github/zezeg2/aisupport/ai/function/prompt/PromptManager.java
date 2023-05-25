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

    public Prompt getPrompt(String functionName){
        return context.getPrompt(functionName);
    }

    public void initPromptContext(String functionName, Prompt prompt) throws Exception {
        if (!context.containsPrompt(functionName)) {
            context.addPromptToContext(functionName, prompt);
        }
        Map<String, List<ChatMessage>> promptMessageContext = prompt.getPromptMessageContext();
        if (!promptMessageContext.containsKey(identifierProvider.getId())) {
            addMessage(functionName, ROLE.SYSTEM, prompt.toString());
        }
    }
    public void addMessage(String functionName, ROLE role, String message){
        String identifier = identifierProvider.getId();
        Map<String, List<ChatMessage>> promptMessageContext = context.getPrompt(functionName).getPromptMessageContext();
        if (!promptMessageContext.containsKey(identifier))
            promptMessageContext.put(identifier, new CopyOnWriteArrayList<>());

        List<ChatMessage> chatMessages = promptMessageContext.get(identifier);
        if (!chatMessages.isEmpty()) chatMessages.add(new ChatMessage(role.getValue(), message));
        else {
            if (role.equals(ROLE.SYSTEM)) chatMessages.add(new ChatMessage(role.getValue(), message));
            else throw new NotInitiatedContextException();
        }
    }

    public ChatMessage exchangeMessages(String functionName, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = context.getPromptMessageContext(functionName).get(identifierProvider.getId());
        ChatCompletionResult response = createChatCompletion(model, contextMessages);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        if (save) contextMessages.add(responseMessage);
        return responseMessage;
    }

    public ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build());
    }
}
