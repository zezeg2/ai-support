package io.github.zezeg2.aisupport.core.reactive.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ReactivePromptManager {
    protected final OpenAiService service;
    protected final ReactivePromptContextHolder context;
    protected final ContextProperties contextProperties;

    public Mono<Void> addMessage(String identifier, String namespace, ROLE role, String message, ContextType contextType) {
        return addMessageToContext(identifier, namespace, role, message, contextType);
    }

    protected Mono<Void> addMessageToContext(String identifier, String namespace, ROLE role, String message, ContextType contextType) {
        return Mono.just(identifier).flatMap(id -> switch (contextType) {
            case PROMPT -> context.savePromptMessages(namespace, id, new ChatMessage(role.getValue(), message));
            case FEEDBACK -> context.saveFeedbackMessages(namespace, id, new ChatMessage(role.getValue(), message));
        });

    }

    public Mono<ChatCompletionResult> exchangePromptMessages(String identifier, String namespace, AIModel model, boolean save) {
        return Mono.just(identifier)
                .flatMap(id -> context.getPromptChatMessages(namespace, id))
                .flatMap(contextMessages -> getChatCompletionResult(identifier, namespace, model, save, contextMessages.getContent(), ContextType.PROMPT));
    }

    public Mono<ChatCompletionResult> exchangeFeedbackMessages(String identifier, String namespace, AIModel model, boolean save) {
        return Mono.just(identifier)
                .flatMap(id -> context.getFeedbackChatMessages(namespace, id))
                .flatMap(contextMessages -> getChatCompletionResult(identifier, namespace, model, save, contextMessages.getContent(), ContextType.FEEDBACK));
    }

    protected Mono<ChatCompletionResult> getChatCompletionResult(String identifier, String namespace, AIModel model, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        return createChatCompletion(model, contextMessages)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
                    return Mono.just(response);
                })
                .flatMap(response -> {
                    if (save) {
                        return Mono.just(identifier)
                                .flatMap(id -> switch (contextType) {
                                    case PROMPT ->
                                            context.savePromptMessages(namespace, id, response.getChoices().get(0).getMessage());
                                    case FEEDBACK ->
                                            context.saveFeedbackMessages(namespace, id, response.getChoices().get(0).getMessage());
                                })
                                .thenReturn(response);
                    }
                    return Mono.just(response);
                });
    }

    protected Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build()));
    }
}
