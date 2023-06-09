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

/**
 * The PromptManager class is responsible for managing prompts and exchanging messages in a chat-based AI system.
 *
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public class PromptManager {

    private final OpenAiService service;
    private final PromptContextHolder context;
    private final ContextProperties contextProperties;

    /**
     * Adds a message to the prompt context.
     *
     * @param namespace   The namespace of the prompt.
     * @param identifier  The identifier of the chat context.
     * @param role        The role of the chat message (e.g., user, assistant).
     * @param message     The content of the chat message.
     * @param contextType The type of context (prompt or feedback).
     */
    public void addMessage(String namespace, String identifier, ROLE role, String message, ContextType contextType) {
        addMessageToContext(namespace, identifier, role, message, contextType);
    }

    /**
     * Adds a message to the prompt context.
     *
     * @param namespace   The namespace of the prompt.
     * @param identifier  The identifier of the chat context.
     * @param role        The role of the chat message (e.g., user, assistant).
     * @param message     The content of the chat message.
     * @param contextType The type of context (prompt or feedback).
     */
    protected void addMessageToContext(String namespace, String identifier, ROLE role, String message, ContextType contextType) {
        switch (contextType) {
            case PROMPT:
                context.savePromptMessages(namespace, identifier, new ChatMessage(role.getValue(), message));
                break;
            case FEEDBACK:
                context.saveFeedbackMessages(namespace, identifier, new ChatMessage(role.getValue(), message));
                break;
        }
    }

    /**
     * Exchanges prompt messages with the AI model and returns the chat completion result.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the chat context.
     * @param model      The AI model to use for the chat completion.
     * @param topP       The top-p value for generating diverse completions.
     * @param save       Specifies whether to save the generated response in the prompt context.
     * @return The chat completion result.
     */
    public ChatCompletionResult exchangePromptMessages(String namespace, String identifier, AIModel model, double topP, boolean save) {
        List<ChatMessage> contextMessages = context.getPromptChatMessages(namespace, identifier).getContent();
        return getChatCompletionResult(namespace, identifier, model, topP, save, contextMessages, ContextType.PROMPT);
    }

    /**
     * Exchanges feedback messages with the AI model and returns the chat completion result.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the chat context.
     * @param model      The AI model to use for the chat completion.
     * @param topP       The top-p value for generating diverse completions.
     * @param save       Specifies whether to save the generated response in the prompt context.
     * @return The chat completion result.
     */
    public ChatCompletionResult exchangeFeedbackMessages(String namespace, String identifier, AIModel model, double topP, boolean save) {
        List<ChatMessage> contextMessages = context.getFeedbackChatMessages(namespace, identifier).getContent();
        return getChatCompletionResult(namespace, identifier, model, topP, save, contextMessages, ContextType.FEEDBACK);
    }

    /**
     * Retrieves the chat completion result using the AI model and chat messages.
     * /**
     * Retrieves the chat completion result using the AI model and chat messages.
     *
     * @param namespace       The namespace of the prompt.
     * @param identifier      The identifier of the chat context.
     * @param model           The AI model to use for the chat completion.
     * @param topP            The top-p value for generating diverse completions.
     * @param save            Specifies whether to save the generated response in the prompt context.
     * @param contextMessages The list of chat messages from the prompt context.
     * @param contextType     The type of context (prompt or feedback).
     * @return The chat completion result.
     */
    protected ChatCompletionResult getChatCompletionResult(String namespace, String identifier, AIModel model, double topP, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        ChatCompletionResult response = createChatCompletion(model, contextMessages, topP);
        ChatMessage responseMessage = response.getChoices().get(0).getMessage();
        responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
        if (save) {
            switch (contextType) {
                case PROMPT:
                    context.savePromptMessages(namespace, identifier, responseMessage);
                    break;
                case FEEDBACK:
                    context.saveFeedbackMessages(namespace, identifier, responseMessage);
                    break;
            }
            contextMessages.add(responseMessage);
        }
        return response;
    }

    /**
     * Creates a chat completion request using the AI model and chat messages.
     *
     * @param model    The AI model to use for the chat completion.
     * @param messages The list of chat messages.
     * @param topP     The top-p value for generating diverse completions.
     * @return The chat completion result.
     */
    protected ChatCompletionResult createChatCompletion(AIModel model, List<ChatMessage> messages, double topP) {
        return service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .topP(topP)
                .build());
    }
}
