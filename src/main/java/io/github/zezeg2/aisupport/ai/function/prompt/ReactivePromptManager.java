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
import io.github.zezeg2.aisupport.context.reactive.ReactiveContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@RequiredArgsConstructor
public abstract class ReactivePromptManager<S> {
    private final OpenAiService service;
    private final ReactivePromptContextHolder<S> context;
    private final ReactiveContextIdentifierProvider<S> identifierProvider;
    private final ContextProperties contextProperties;

    public Mono<Void> initPromptContext(S idSource, String functionName, ReactivePrompt<S> prompt) {
        return context.savePromptToContext(functionName, prompt)
                .then(getIdentifier(idSource))
                .flatMap(identifier -> {
                    Map<String, List<ChatMessage>> promptMessageContext = prompt.getPromptMessageContext();
                    if (!promptMessageContext.containsKey(identifier)) {
                        return addMessage(idSource, functionName, ROLE.SYSTEM, prompt.toString());
                    }
                    return Mono.empty();
                });
    }


    public Mono<Void> initPromptContext(S idSource, String functionName) {
        return context.getPrompt(functionName).publishOn(Schedulers.boundedElastic()).log().flatMap(prompt -> initPromptContext(idSource, functionName, prompt));
    }

    public Mono<Void> initMessageContext(S idSource, String functionName, String systemMessage, Map<String, List<ChatMessage>> messageContext) {
        return getIdentifier(idSource).log().flatMap(identifier -> {
            if (!messageContext.containsKey(identifier)) {
                return addMessage(idSource, functionName, ROLE.SYSTEM, systemMessage, messageContext);
            }
            return Mono.empty();
        }).log();
    }

    public Mono<Void> addMessage(S idSource, String functionName, ROLE role, String message) {
        return getIdentifier(idSource)
                .flatMap(identifier ->
                        context.getPrompt(functionName)
                                .doOnNext(prompt -> {
                                    Map<String, List<ChatMessage>> messageContext = prompt.getPromptMessageContext();
                                    addMessageToContext(messageContext, functionName, identifier, role, message)
                                            .subscribe(); // You should ideally avoid subscribe() here, but without seeing the method's implementation, I'm not sure how best to refactor this
                                })
                )
                .then();
    }

    public Mono<Void> addMessage(S idSource, String functionName, ROLE role, String message, Map<String, List<ChatMessage>> messageContext) {
        return getIdentifier(idSource).log().flatMap(identifier -> addMessageToContext(messageContext, functionName, identifier, role, message)).log();
    }

    private Mono<Void> addMessageToContext(Map<String, List<ChatMessage>> messageContext, String functionName, String identifier, ROLE role, String message) {
        if (!messageContext.containsKey(identifier)) {
            messageContext.put(identifier, new CopyOnWriteArrayList<>());
            redisPersistenceSupport(functionName)
                    .subscribe(); // Same as above, try to avoid subscribe() in the middle of a pipeline
        }

        List<ChatMessage> chatMessages = messageContext.get(identifier);
        if (chatMessages.isEmpty() && !role.equals(ROLE.SYSTEM)) {
            return Mono.error(new NotInitiatedContextException());
        } else {
            chatMessages.add(new ChatMessage(role.getValue(), message));
            return Mono.empty();
        }
    }

    private Mono<Void> redisPersistenceSupport(String functionName) {
        if (functionName != null && contextProperties.getContext().equals(ContextProperties.CONTEXT.REDIS)) {
            return context.getPrompt(functionName)
                    .flatMap(prompt -> context.savePromptToContext(functionName, prompt));
        }
        return Mono.empty();
    }


    public Mono<ChatCompletionResult> exchangeMessages(S idSource, String functionName, AIModel model, boolean save) {
        return getIdentifier(idSource)
                .log().flatMap(identifier -> context.getPrompt(functionName).doOnNext(System.out::println)
                        .log().map(prompt -> prompt.getPromptMessageContext().get(identifier))
                        .log().flatMap(contextMessages -> getChatCompletionResult(functionName, model, save, (List<ChatMessage>) contextMessages))).log();
    }

    public Mono<ChatCompletionResult> exchangeMessages(S idSource, String functionName, Map<String, List<ChatMessage>> messageContext, AIModel model, boolean save) {
        return getIdentifier(idSource).flatMap(identifier -> {
            List<ChatMessage> contextMessages = messageContext.get(identifier);
            return getChatCompletionResult(functionName, model, save, contextMessages);
        });
    }

    private Mono<ChatCompletionResult> getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages) {
        return createChatCompletion(model, contextMessages).log().flatMap(response -> {
            ChatMessage responseMessage = response.getChoices().get(0).getMessage();
            responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
            if (save) {
                contextMessages.add(responseMessage);
                redisPersistenceSupport(functionName);
            }
            return Mono.just(response);
        }).log();
    }

    public Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build())).log();
    }

    public Mono<String> getIdentifier(S idSource) {
        return identifierProvider.getId(idSource).log();
    }
}

