package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    protected Mono<Void> init(String functionName) {
        return promptManager.getIdentifier().flatMap(identifier -> promptManager.getContext().getFeedbackChatMessages(getName(functionName), identifier))
                .doOnNext(feedbackChatMessages -> {
                    if (feedbackChatMessages.isEmpty()) {
                        promptManager.addMessage(functionName, ROLE.SYSTEM, buildTemplate(functionName), ContextType.FEEDBACK);
                    }
                }).then();

    }

    private String buildTemplate(String functionName) {
        String FEEDBACK_FRAME = """
                You are tasked with inspecting the provided Json and please provide feedback according to the given `Feedback Format`
                            
                Feedback Format:
                ```json
                %s
                ```
                                
                The inspection items are as follows.
                %s
                            
                            
                Do not include any other explanatory text in your response other than result
                """;
        return FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), addContents(functionName));
    }

    public Mono<String> validate(String functionName) {
        AtomicInteger counter = new AtomicInteger(0);
        return init(functionName)
                .then(Flux.defer(() -> getLastPromptResponseContent(functionName))
                        .expand(lastResponseContent -> Mono.defer(() -> getResponseContent(functionName, lastResponseContent, ContextType.FEEDBACK))
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
                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(functionName)))
                                .repeat(MAX_ATTEMPTS - 1))
                        .onErrorResume(e -> Mono.just(e.getMessage()))
                        .last());
    }

    protected Mono<String> getResponseContent(String functionName, String message, ContextType contextType) {
        promptManager.addMessage(functionName, ROLE.USER, message, contextType);
        return switch (contextType) {
            case PROMPT ->
                    promptManager.exchangePromptMessages(functionName, GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
            case FEEDBACK ->
                    promptManager.exchangeFeedbackMessages(getName(functionName), GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
        };
    }

    protected Mono<String> getLastPromptResponseContent(String functionName) {
        return promptManager.getIdentifier()
                .flatMap(identifier -> promptManager.getContext().getPromptChatMessages(functionName, identifier))
                .flatMap(promptMessageList -> Mono.just(promptMessageList.get(promptMessageList.size() - 1).getContent()));
    }

    protected abstract String addContents(String functionName);
}
