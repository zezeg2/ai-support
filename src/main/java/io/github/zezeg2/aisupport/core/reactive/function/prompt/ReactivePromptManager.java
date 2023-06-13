package io.github.zezeg2.aisupport.core.reactive.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.reactive.ReactiveContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ReactivePromptManager {
    protected final OpenAiService service;
    protected final ReactivePromptContextHolder context;
    protected final ReactiveContextIdentifierProvider identifierProvider;
    protected final ContextProperties contextProperties;

    public Mono<String> getIdentifier(ServerWebExchange exchange) {
        return identifierProvider.getId(exchange);
    }

    public Mono<Void> addMessage(ServerWebExchange exchange, String namespace, ROLE role, String message, ContextType contextType) {
        return addMessageToContext(exchange, namespace, role, message, contextType);
    }

    protected Mono<Void> addMessageToContext(ServerWebExchange exchange, String namespace, ROLE role, String message, ContextType contextType) {
        return getIdentifier(exchange).flatMap(identifier -> switch (contextType) {
            case PROMPT ->
                    context.savePromptMessages(namespace, identifier, new ChatMessage(role.getValue(), message));
            case FEEDBACK ->
                    context.saveFeedbackMessages(namespace, identifier, new ChatMessage(role.getValue(), message));
        });

    }

    public Mono<ChatCompletionResult> exchangePromptMessages(ServerWebExchange exchange, String namespace, AIModel model, boolean save) {
        return getIdentifier(exchange)
                .flatMap(identifier -> context.getPromptChatMessages(namespace, identifier))
                .flatMap(contextMessages -> getChatCompletionResult(exchange, namespace, model, save, contextMessages.getContent(), ContextType.PROMPT));
    }

    public Mono<ChatCompletionResult> exchangeFeedbackMessages(ServerWebExchange exchange, String namespace, AIModel model, boolean save) {
        return getIdentifier(exchange)
                .flatMap(identifier -> context.getFeedbackChatMessages(namespace, identifier))
                .flatMap(contextMessages -> getChatCompletionResult(exchange, namespace, model, save, contextMessages.getContent(), ContextType.FEEDBACK));
    }

    protected Mono<ChatCompletionResult> getChatCompletionResult(ServerWebExchange exchange, String namespace, AIModel model, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        return createChatCompletion(model, contextMessages)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
                    return Mono.just(response);
                })
                .flatMap(response -> {
                    if (save) {
                        return getIdentifier(exchange)
                                .flatMap(identifier -> switch (contextType) {
                                    case PROMPT ->
                                            context.savePromptMessages(namespace, identifier, response.getChoices().get(0).getMessage());
                                    case FEEDBACK ->
                                            context.saveFeedbackMessages(namespace, identifier, response.getChoices().get(0).getMessage());
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
