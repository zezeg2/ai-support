package io.github.zezeg2.aisupport.core.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.core.validator.ResultValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ReactiveResultValidator extends ResultValidator<Mono<String>, ReactivePromptManager> {

    public ReactiveResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper) {
        super(promptManager, mapper);
    }

    @Override
    public Mono<String> validate(String functionName) {
        init(functionName);
        AtomicInteger counter = new AtomicInteger(0);

        return Flux.defer(() -> getLastPromptResponseContent(functionName))
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
                .last();
    }


    @Override
    protected Mono<String> getResponseContent(String functionName, String message, ContextType contextType) {
        promptManager.addMessage(functionName, ROLE.USER, message, contextType);
        return switch (contextType) {
            case PROMPT ->
                    promptManager.exchangePromptMessages(functionName, GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
            case FEEDBACK ->
                    promptManager.exchangeFeedbackMessages(getName(functionName), GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
        };
    }

    @Override
    protected Mono<String> getLastPromptResponseContent(String functionName) {
        List<ChatMessage> promptMessageList = promptManager.getContext().getPromptChatMessages(functionName, promptManager.getIdentifier());
        return Mono.just(promptMessageList.get(promptMessageList.size() - 1).getContent());
    }
}
