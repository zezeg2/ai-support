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
import io.github.zezeg2.aisupport.core.function.prompt.MessageContext;
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
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext Message context for calling openai chat completion api.
     * @param role           The role of the chat message (e.g., user, assistant).
     * @param message        The content of the chat message.
     * @return A Mono representing the completion of the operation.
     */

    public Mono<Void> addMessageToContext(ContextType contextType, MessageContext messageContext, ROLE role, String message) {
        messageContext.getMessages().add(new ChatMessage(role.getValue(), message));
        return contextHolder.saveMessageContext(contextType, messageContext);
    }

    /**
     * Exchange messages in the chat-based AI system and retrieve the chat completion result.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext Message context for calling openai chat completion api.
     * @param model          The AI model to use for the chat completion.
     * @param topP           The top-p value for generating diverse completions.
     * @param save           Specifies whether to save the generated response in the prompt context.
     * @return A Mono containing the chat completion result.
     */

    public <T extends MessageContext> Mono<T> exchangeMessages(ContextType contextType, MessageContext messageContext, AIModel model, double topP, boolean save) {
        return createChatCompletion(model, messageContext.getMessages(), topP)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    responseMessage.setContent(JsonUtil.extractJsonFromMessage(responseMessage.getContent()));
                    messageContext.getMessages().add(responseMessage);
                    if (save) {
                        return contextHolder.saveMessageContext(contextType, messageContext).then(Mono.just((T) messageContext));
                    }
                    return Mono.just((T) messageContext);
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
