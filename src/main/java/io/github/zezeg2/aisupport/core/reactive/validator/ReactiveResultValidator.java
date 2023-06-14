package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "EVENTLOOP")
public abstract class ReactiveResultValidator {
    protected static final int MAX_ATTEMPTS = 3;
    protected final ReactivePromptManager promptManager;
    protected final ObjectMapper mapper;

    public ReactiveResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
    }

    protected String getNamespace(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName()));
    }

    protected Mono<Void> init(ServerWebExchange exchange, String functionName) {
        return promptManager.getIdentifier(exchange)
                .flatMap(identifier -> promptManager.getContext().getFeedbackChatMessages(getNamespace(functionName), identifier)
                        .flatMap(feedbackChatMessages -> {
                            if (feedbackChatMessages.getContent().isEmpty()) {
                                return buildTemplate(functionName)
                                        .flatMap(template -> promptManager.addMessage(exchange, getNamespace(functionName), ROLE.SYSTEM, template, ContextType.FEEDBACK));
                            }
                            return Mono.empty();
                        }));
    }

    private Mono<String> buildTemplate(String functionName) {
        return addContents(functionName).map(content -> TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), content));
    }

    public Mono<String> validate(ServerWebExchange exchange, String functionName) {
        return init(exchange, functionName)
                .then(Mono.defer(() -> getLastPromptResponseContent(exchange, functionName))
                        .flatMap(lastResponseContent -> Mono.defer(() -> exchangeMessages(exchange, functionName, lastResponseContent, ContextType.FEEDBACK))
                                .flatMap(lastFeedbackContent -> {
                                    FeedbackResponse feedbackResult;
                                    try {
                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                    } catch (JsonProcessingException e) {
                                        return exchangeMessages(exchange, functionName, lastResponseContent, ContextType.FEEDBACK).doOnNext(ignored -> Mono.error(new RuntimeException(e)));
                                    }

                                    if (feedbackResult.isValid()) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.defer(() -> {
                                            Mono<String> result = exchangeMessages(exchange, functionName, lastFeedbackContent, ContextType.PROMPT);
                                            return result.flatMap(r -> Mono.error(new RuntimeException()));
                                        });
                                    }
                                })
                                .retry(MAX_ATTEMPTS - 1)
                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(exchange, functionName)))));
    }


    protected Mono<String> exchangeMessages(ServerWebExchange exchange, String functionName, String message, ContextType contextType) {
        return promptManager.addMessage(exchange, contextType.equals(ContextType.PROMPT) ? functionName : getNamespace(functionName), ROLE.USER, message, contextType)
                .then(switch (contextType) {
                    case PROMPT ->
                            promptManager.exchangePromptMessages(exchange, functionName, GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                    case FEEDBACK ->
                            promptManager.exchangeFeedbackMessages(exchange, getNamespace(functionName), GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                });
    }

    protected Mono<String> getLastPromptResponseContent(ServerWebExchange exchange, String functionName) {
        return promptManager.getIdentifier(exchange)
                .flatMap(identifier -> promptManager.getContext().getPromptChatMessages(functionName, identifier))
                .flatMap(promptMessageList -> Mono.just(promptMessageList.getContent().get(promptMessageList.getContent().size() - 1).getContent()));
    }

    protected abstract Mono<String> addContents(String functionName);
}
