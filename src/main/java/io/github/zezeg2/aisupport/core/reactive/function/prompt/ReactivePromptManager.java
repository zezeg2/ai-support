package io.github.zezeg2.aisupport.core.reactive.function.prompt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.util.JsonUtil;
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
    protected final ReactivePromptContextHolder contextHolder;
    protected final ContextProperties contextProperties;

    public Mono<Void> addMessageToContext(String namespace, String identifier, ROLE role, String message, ContextType contextType) {
        return Mono.defer(() -> contextHolder.saveMessage(contextType, namespace, identifier, new ChatMessage(role.getValue(), message)));
    }

    public Mono<ChatCompletionResult> exchangeMessages(ContextType contextType, String namespace, String identifier, AIModel model, double topP, boolean save) {
        return contextHolder.getContext(contextType, namespace, identifier)
                .flatMap(context -> getChatCompletionResult(namespace, identifier, model, topP, save, context.getMessages(), contextType));
    }

    protected Mono<ChatCompletionResult> getChatCompletionResult(String namespace, String identifier, AIModel model, double topP, boolean save, List<ChatMessage> contextMessages, ContextType contextType) {
        return createChatCompletion(model, contextMessages, topP)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    if (save)
                        return addMessageToContext(namespace, identifier, ROLE.ASSISTANT, JsonUtil.extractJsonFromMessage(responseMessage.getContent()), contextType).thenReturn(response);
                    return Mono.just(response);
                });
    }

    protected Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages, double topP) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .topP(topP)
                .build()));
    }
}
