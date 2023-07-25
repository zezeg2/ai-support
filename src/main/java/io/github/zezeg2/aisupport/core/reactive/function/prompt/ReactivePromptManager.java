package io.github.zezeg2.aisupport.core.reactive.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.util.JsonUtil;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The ReactivePromptManager class is responsible for managing prompts and exchanging messages in a chat-based AI system in a reactive manner.
 */
@RequiredArgsConstructor
@Getter
public class ReactivePromptManager {
    protected final OpenAiService service;
    protected final ReactivePromptContextHolder contextHolder;
    protected final ContextProperties contextProperties;

    /**
     * Adds a message to the prompt context.
     *
     * @param namespace   The namespace of the prompt.
     * @param identifier  The identifier of the chat context.
     * @param role        The role of the chat message (e.g., user, assistant).
     * @param message     The content of the chat message.
     * @param contextType The type of context (prompt or feedback).
     * @return A Mono representing the completion of the operation.
     */
    public Mono<Void> addMessageToContext(String namespace, String identifier, ROLE role, String message, ContextType contextType) {
        return Mono.defer(() -> contextHolder.saveMessage(contextType, namespace, identifier, new ChatMessage(role.getValue(), message)));
    }

    /**
     * Exchange messages in the chat-based AI system and retrieve the chat completion result.
     *
     * @param contextType The type of context (prompt or feedback).
     * @param namespace   The namespace of the prompt.
     * @param identifier  The identifier of the chat context.
     * @param model       The AI model to use for the chat completion.
     * @param topP        The top-p value for generating diverse completions.
     * @param save        Specifies whether to save the generated response in the prompt context.
     * @return A Mono containing the chat completion result.
     */
    public Mono<ChatCompletionResult> exchangeMessages(ContextType contextType, String namespace, String identifier, AIModel model, double topP, boolean save) {
        return contextHolder.getContext(contextType, namespace, identifier)
                .flatMap(context -> getChatCompletionResult(namespace, identifier, model, topP, save, context.getMessages(), contextType));
    }

    /**
     * Retrieves the chat completion result using the AI model and chat messages.
     *
     * @param namespace       The namespace of the prompt.
     * @param identifier      The identifier of the chat context.
     * @param model           The AI model to use for the chat completion.
     * @param topP            The top-p value for generating diverse completions.
     * @param save            Specifies whether to save the generated response in the prompt context.
     * @param contextMessages The list of chat messages from the prompt context.
     * @param contextType     The type of context (prompt or feedback).
     * @return A Mono containing the chat completion result.
     */
    protected Mono<ChatCompletionResult> getChatCompletionResult(String namespace, String identifier, AIModel model, double topP, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        return createChatCompletion(model, contextMessages, topP)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    if (save)
                        return addMessageToContext(namespace, identifier, ROLE.ASSISTANT, JsonUtil.extractJsonFromMessage(responseMessage.getContent()), contextType).thenReturn(response);
                    return Mono.just(response);
                });
    }

    /**
     * Creates a chat completion request using the AI model and chat messages.
     *
     * @param model    The AI model to use for the chat completion.
     * @param messages The list of chat messages.
     * @param topP     The top-p value for generating diverse completions.
     * @return A Mono containing the chat completion result.
     */
    protected Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages, double topP) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .topP(topP)
                .build()));
    }
}
