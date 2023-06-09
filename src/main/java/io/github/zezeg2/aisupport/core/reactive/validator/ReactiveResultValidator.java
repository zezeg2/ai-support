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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "EVENTLOOP")
public abstract class ReactiveResultValidator {
    protected static final int MAX_ATTEMPTS = 3;
    protected final ReactivePromptManager promptManager;
    protected final ObjectMapper mapper;

    public ReactiveResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
    }

    protected String getName(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName().toLowerCase()));
    }

    protected Mono<Void> init(ServerWebExchange exchange, String functionName) {
        return promptManager.getIdentifier(exchange).flatMap(identifier -> promptManager.getContext().getFeedbackChatMessages(getName(functionName), identifier))
                .doOnNext(feedbackChatMessages -> {
                    if (feedbackChatMessages.isEmpty()) {
                        buildTemplate(functionName).doOnNext(template -> {
                            promptManager.addMessage(exchange, functionName, ROLE.SYSTEM, template, ContextType.FEEDBACK);
                        }).then();
                    }
                }).then();

    }

    private Mono<String> buildTemplate(String functionName) {
        return addContents(functionName).map(content -> TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), content));
    }

    public Mono<String> validate(ServerWebExchange exchange, String functionName) {
        AtomicInteger counter = new AtomicInteger(0);
        return init(exchange, functionName)
                .then(Flux.defer(() -> getLastPromptResponseContent(exchange, functionName))
                        .expand(lastResponseContent -> Mono.defer(() -> getResponseContent(exchange, functionName, lastResponseContent, ContextType.FEEDBACK))
                                .flatMap(lastFeedbackContent -> {
                                    FeedbackResponse feedbackResult;
                                    try {
                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(new RuntimeException(e));
                                    }

                                    if (feedbackResult.isValid()) return Mono.empty();
                                    else {
                                        if (counter.incrementAndGet() >= MAX_ATTEMPTS) {
                                            return Mono.error(new RuntimeException("Exceeded maximum attempts"));
                                        }
                                        return Mono.just(lastFeedbackContent);
                                    }
                                })
                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(exchange, functionName)))
                                .repeat(MAX_ATTEMPTS - 1))
                        .onErrorResume(e -> Mono.just(e.getMessage()))
                        .last());
    }

    protected Mono<String> getResponseContent(ServerWebExchange exchange, String functionName, String message, ContextType contextType) {
        return promptManager.addMessage(exchange, functionName, ROLE.USER, message, contextType)
                .then(switch (contextType) {
                    case PROMPT ->
                            promptManager.exchangePromptMessages(exchange, functionName, GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                    case FEEDBACK ->
                            promptManager.exchangeFeedbackMessages(exchange, getName(functionName), GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                });

    }

    protected Mono<String> getLastPromptResponseContent(ServerWebExchange exchange, String functionName) {
        return promptManager.getIdentifier(exchange)
                .flatMap(identifier -> promptManager.getContext().getPromptChatMessages(functionName, identifier))
                .flatMap(promptMessageList -> Mono.just(promptMessageList.get(promptMessageList.size() - 1).getContent()));
    }

    protected abstract Mono<String> addContents(String functionName);
}
