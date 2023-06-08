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
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ReactivePromptManager {
    protected final OpenAiService service;
    protected final ReactivePromptContextHolder context;
    protected final ReactiveContextIdentifierProvider identifierProvider;
    protected final ContextProperties contextProperties;

    public Mono<String> getIdentifier() {
        return identifierProvider.getId();
    }

    public Mono<Void> addMessage(String functionName, ROLE role, String message, ContextType contextType) {
        return addMessageToContext(functionName, role, message, contextType);
    }

    protected Mono<Void> addMessageToContext(String functionName, ROLE role, String message, ContextType contextType) {
        return getIdentifier().doOnNext(identifier -> {
            switch (contextType) {
                case PROMPT ->
                        context.savePromptMessagesContext(functionName, identifier, new ChatMessage(role.getValue(), message));
                case FEEDBACK ->
                        context.saveFeedbackMessagesContext(functionName, identifier, new ChatMessage(role.getValue(), message));
            }
        }).then();

    }

    public Mono<ChatCompletionResult> exchangePromptMessages(String functionName, AIModel model, boolean save) {
        return getIdentifier()
                .flatMap(identifier -> context.getPromptChatMessages(functionName, identifier))
                .flatMap(contextMessages -> getChatCompletionResult(functionName, model, save, contextMessages, ContextType.PROMPT));
    }

    public Mono<ChatCompletionResult> exchangeFeedbackMessages(String validatorName, AIModel model, boolean save) {
        return getIdentifier()
                .flatMap(identifier -> context.getFeedbackChatMessages(validatorName, identifier))
                .flatMap(contextMessages -> getChatCompletionResult(validatorName, model, save, contextMessages, ContextType.FEEDBACK));
    }

    protected Mono<ChatCompletionResult> getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        return createChatCompletion(model, contextMessages)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
                    if (save) {
                        getIdentifier().flatMap(identifier -> switch (contextType) {
                            case PROMPT -> context.savePromptMessagesContext(functionName, identifier, responseMessage);
                            case FEEDBACK ->
                                    context.saveFeedbackMessagesContext(functionName, identifier, responseMessage);
                        }).then();
                    }
                    return Mono.just(response);
                });
    }

    protected Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build())).log();
    }
}
