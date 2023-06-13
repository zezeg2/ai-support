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
        return promptManager.getIdentifier(exchange)
                .flatMap(identifier -> promptManager.getContext().getFeedbackChatMessages(getName(functionName), identifier)
                        .flatMap(feedbackChatMessages -> {
                            if (feedbackChatMessages.isEmpty()) {
                                return buildTemplate(functionName)
                                        .flatMap(template -> promptManager.addMessage(exchange, getName(functionName), ROLE.SYSTEM, template, ContextType.FEEDBACK));
                            }
                            return Mono.empty();
                        }));
    }

    private Mono<String> buildTemplate(String functionName) {
        return addContents(functionName).map(content -> TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), content));
    }

//    public Mono<String> validate(ServerWebExchange exchange, String functionName) {
//        AtomicInteger counter = new AtomicInteger(0);
//        return init(exchange, functionName)
//                .then(Flux.from(getLastPromptResponseContent(exchange, functionName))
//                        .expand(lastResponseContent -> Mono.from(exchangeMessages(exchange, functionName, lastResponseContent, ContextType.FEEDBACK))
//                                .flatMap(lastFeedbackContent -> {
//                                    FeedbackResponse feedbackResult;
//                                    try {
//                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
//                                    } catch (JsonProcessingException e) {
//                                        return Mono.error(new RuntimeException(e));
//                                    }
//
//                                    if (feedbackResult.isValid()) return Mono.empty();
//                                    else {
//                                        if (counter.incrementAndGet() >= MAX_ATTEMPTS) {
//                                            return Mono.error(new RuntimeException("Exceeded maximum attempts"));
//                                        }
//                                        return Mono.just(lastFeedbackContent);
//                                    }
//                                })
//                                .flatMap(lastFeedbackContent -> Mono.from(exchangeMessages(exchange, functionName, lastFeedbackContent, ContextType.PROMPT)))
//                                .switchIfEmpty(Mono.from(getLastPromptResponseContent(exchange, functionName)))
//                                .repeat(MAX_ATTEMPTS - 1)
//                        )
//                        .onErrorResume(e -> Mono.just(e.getMessage()))
//                        .last());
//
//
//    }

//    public Mono<String> validate(ServerWebExchange exchange, String functionName) {
//        AtomicInteger counter = new AtomicInteger(0);
//        AtomicBoolean shouldRepeat = new AtomicBoolean(true);
//
//        return init(exchange, functionName)
//                .then(Flux.defer(() -> getLastPromptResponseContent(exchange, functionName))
//                        .expand(lastResponseContent -> Mono.defer(() -> exchangeMessages(exchange, functionName, lastResponseContent, ContextType.FEEDBACK))
//                                .flatMap(lastFeedbackContent -> {
//                                    if (counter.incrementAndGet() > MAX_ATTEMPTS) {
//                                        shouldRepeat.set(false);
//                                    }
//
//                                    FeedbackResponse feedbackResult;
//                                    try {
//                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
//                                    } catch (JsonProcessingException e) {
//                                        return Mono.error(new RuntimeException(e));
//                                    }
//
//                                    if (feedbackResult.isValid()) {
//                                        shouldRepeat.set(false);
//                                        return Mono.empty();
//                                    } else {
//                                        return Mono.just(lastFeedbackContent);
//                                    }
//                                })
//                                .flatMap(lastFeedbackContent -> Mono.defer(() -> exchangeMessages(exchange, functionName, lastFeedbackContent, ContextType.PROMPT)))
//                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(exchange, functionName).log()))
//                        ).last());
//    }


    public Mono<String> validate(ServerWebExchange exchange, String functionName) {
        AtomicInteger counter = new AtomicInteger(0);

        return init(exchange, functionName)
                .then(Mono.defer(() -> getLastPromptResponseContent(exchange, functionName))
                        .flatMap(lastResponseContent -> Mono.defer(() -> exchangeMessages(exchange, functionName, lastResponseContent, ContextType.FEEDBACK))
                                .flatMap(lastFeedbackContent -> {
                                    if (counter.incrementAndGet() > MAX_ATTEMPTS) {
                                        return Mono.error(new RuntimeException("Exceeded maximum attempts"));
                                    }

                                    FeedbackResponse feedbackResult;
                                    try {
                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(new RuntimeException(e));
                                    }

                                    if (feedbackResult.isValid()) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.defer(() -> {
                                            Mono<String> result = exchangeMessages(exchange, functionName, lastFeedbackContent, ContextType.PROMPT);
                                            return result.flatMap(r -> Mono.<String>error(new RuntimeException()));
                                        });
                                    }
                                })
                                .retry()
                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(exchange, functionName))).log()));
    }


    protected Mono<String> exchangeMessages(ServerWebExchange exchange, String functionName, String message, ContextType contextType) {
        return promptManager.addMessage(exchange, contextType.equals(ContextType.PROMPT) ? functionName : getName(functionName), ROLE.USER, message, contextType)
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
