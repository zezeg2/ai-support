package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.common.JsonUtils;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.context.servlet.ContextIdentifierProvider;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactivePromptManager extends PromptManager<Mono<ChatCompletionResult>> {

    public ReactivePromptManager(OpenAiService service, PromptContextHolder context, ContextIdentifierProvider identifierProvider, ContextProperties contextProperties) {
        super(service, context, identifierProvider, contextProperties);
    }

    @Override
    public Mono<ChatCompletionResult> exchangePromptMessages(String functionName, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = context.getPromptChatMessages(functionName, getIdentifier());
        return getChatCompletionResult(model, save, contextMessages);
    }

    @Override
    public Mono<ChatCompletionResult> exchangeFeedbackMessages(String validatorName, AIModel model, boolean save) {
        List<ChatMessage> contextMessages = context.getFeedbackChatMessages(validatorName, getIdentifier());
        return getChatCompletionResult(model, save, contextMessages);
    }

    @Override
    protected Mono<ChatCompletionResult> getChatCompletionResult(AIModel model, boolean save, List<ChatMessage> contextMessages) {
        return createChatCompletion(model, contextMessages)
                .flatMap(response -> {
                    ChatMessage responseMessage = response.getChoices().get(0).getMessage();
                    responseMessage.setContent(JsonUtils.extractJsonFromMessage(responseMessage.getContent()));
                    if (save) contextMessages.add(responseMessage);
                    return Mono.just(response);
                });
    }

    @Override
    protected Mono<ChatCompletionResult> createChatCompletion(AIModel model, List<ChatMessage> messages) {
        return Mono.fromCallable(() -> service.createChatCompletion(ChatCompletionRequest.builder()
                .model(model.getValue())
                .messages(messages)
                .build())).log();
    }
}
