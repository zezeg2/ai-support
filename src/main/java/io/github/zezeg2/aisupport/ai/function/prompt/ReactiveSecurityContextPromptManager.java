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
import io.github.zezeg2.aisupport.context.reactive.ReactivePromptContextHolder;
import io.github.zezeg2.aisupport.context.reactive.ReactiveSecurityContextIdentifierProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Getter
public class ReactiveSecurityContextPromptManager {
    private final OpenAiService service;
    private final ReactivePromptContextHolder context;
    private final ReactiveSecurityContextIdentifierProvider identifierProvider;
    private final ContextProperties contextProperties;


    @Transactional
    public Mono<Void> initPromptContext(String functionName, ReactivePrompt prompt) {
        return context.containsPrompt(functionName).flatMap(exist -> {
            if (!exist) {
                context.savePromptToContext(functionName, prompt);
            }
            Map<String, List<ChatMessage>> promptMessageContext = prompt.getPromptMessageContext();
            getIdentifier().publishOn(Schedulers.boundedElastic()).doOnNext(identifier -> {
                if (!promptMessageContext.containsKey(identifier)) {
                    addMessage(functionName, ROLE.SYSTEM, prompt.toString()).subscribe();
                }
            });
            return Mono.empty();
        });
    }

    @Transactional
    public Mono<Void> initPromptContext(String functionName) {
        return context.getPrompt(functionName).publishOn(Schedulers.boundedElastic()).flatMap(prompt -> initPromptContext(functionName, prompt));
    }

    @Transactional
    public Mono<Void> initMessageContext(String functionName, String systemMessage, Map<String, List<ChatMessage>> messageContext) {
        return getIdentifier().flatMap(identifier -> {
            if (!messageContext.containsKey(identifier)) {
                return addMessage(functionName, ROLE.SYSTEM, systemMessage, messageContext);
            }
            return Mono.empty();
        });
    }

    @Transactional
    public Mono<Void> addMessage(String functionName, ROLE role, String message) {
        return getIdentifier().flatMap(identifier -> {
            context.getPrompt(functionName).doOnNext(prompt -> {
                Map<String, List<ChatMessage>> messageContext = prompt.getPromptMessageContext();
                addMessageToContext(messageContext, functionName, identifier, role, message);
            });
            return Mono.empty();
        });
    }

    public Mono<Void> addMessage(String functionName, ROLE role, String message, Map<String, List<ChatMessage>> messageContext) {
        return getIdentifier().flatMap(identifier -> addMessageToContext(messageContext, functionName, identifier, role, message));
    }

    private Mono<Void> addMessageToContext(Map<String, List<ChatMessage>> messageContext, String functionName, String identifier, ROLE role, String message) {
        if (!messageContext.containsKey(identifier)) {
            messageContext.put(identifier, new CopyOnWriteArrayList<>());
            redisPersistenceSupport(functionName);
        }

        List<ChatMessage> chatMessages = messageContext.get(identifier);
        if (chatMessages.isEmpty() && !role.equals(ROLE.SYSTEM)) {
            return Mono.error(new NotInitiatedContextException());
        } else {
            chatMessages.add(new ChatMessage(role.getValue(), message));
            return Mono.empty();
        }
    }


    private void redisPersistenceSupport(String functionName) {
        if (functionName != null && contextProperties.getContext().equals(ContextProperties.CONTEXT.REDIS)) {
            context.getPrompt(functionName).flatMap(prompt -> context.savePromptToContext(functionName, prompt));
        }
    }


    public Mono<Void> exchangeMessages(String functionName, AIModel model, boolean save) {
        getIdentifier()
                .flatMap(identifier -> context.getPrompt(functionName)
                        .map(prompt -> prompt.getPromptMessageContext().get(identifier))
                        .doOnNext(contextMessages -> getChatCompletionResult(functionName, model, save, contextMessages)));
        return Mono.empty();
    }

    public Mono<Void> exchangeMessages(String functionName, Map<String, List<ChatMessage>> messageContext, AIModel model, boolean save) {
        getIdentifier().flatMap(identifier -> {
            List<ChatMessage> contextMessages = messageContext.get(identifier);
            return getChatCompletionResult(functionName, model, save, contextMessages);
        });
        return Mono.empty();
    }

    private Mono<ChatCompletionResult> getChatCompletionResult(String functionName, AIModel model, boolean save, List<ChatMessage> contextMessages) {
        return createChatCompletion(model, contextMessages).flatMap(response -> {
            ChatMessage responseMessage = response.getChoices().get(0).getMessage();
            responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
            if (save) {
                contextMessages.add(responseMessage);
                redisPersistenceSupport(functionName);
            }
            return Mono.just(response);
        });
    }

    public Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build()));
    }

    public Mono<String> getIdentifier() {
        return identifierProvider.getId();
    }
}


