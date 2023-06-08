package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import org.springframework.data.annotation.Transient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public abstract class ReactiveResultValidator<S> implements ReactiveValidatable<S> {
    @Transient
    protected final ReactivePromptManager<S> promptManager;
    protected final ObjectMapper mapper;
    protected final Map<String, List<ChatMessage>> feedbackMessageContext;
    private static final int MAX_ATTEMPTS = 3;

    public ReactiveResultValidator(ReactivePromptManager<S> promptManager, ObjectMapper mapper, Map<String, List<ChatMessage>> feedbackMessageContext) {
        this.promptManager = promptManager;
        this.mapper = mapper;
        this.feedbackMessageContext = feedbackMessageContext;
    }

    public void initFeedbackMessageContext(S idSource, String functionName) {
        promptManager.initMessageContext(idSource, functionName, buildTemplate(functionName), feedbackMessageContext);
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

    @Override
    public Flux<String> validate(S idSource, String functionName) {
        initFeedbackMessageContext(idSource, functionName);
        return Flux.defer(() -> getLastPromptResponseContent(idSource, functionName))
                .expand(lastPromptMessage -> Mono.defer(() -> getResponseContent(idSource, functionName, lastPromptMessage, ContextType.FEEDBACK))
                        .flatMap(feedbackContent -> {
                            FeedbackResponse feedbackResult;
                            try {
                                feedbackResult = mapper.readValue(feedbackContent, FeedbackResponse.class);
                            } catch (JsonProcessingException e) {
                                return Mono.error(new RuntimeException(e));
                            }

                            if (feedbackResult.isValid()) {
                                return Mono.empty();
                            } else {
                                return Mono.just(feedbackContent);
                            }
                        })
                        .switchIfEmpty(Mono.defer(() -> getResponseContent(idSource, functionName, lastPromptMessage, ContextType.PROMPT)))
                        .repeat(MAX_ATTEMPTS - 1))
                .doOnDiscard(String.class, (message) -> {
                    throw new RuntimeException("Maximum Validate count over");
                });
    }


    private Mono<String> getResponseContent(S idSource, String functionName, String message, ContextType contextType) {
        Mono<Map<String, List<ChatMessage>>> messageContext = switch (contextType) {
            case PROMPT ->
                    promptManager.getContext().getPrompt(functionName).map(ReactivePrompt::getPromptMessageContext);
            case FEEDBACK -> Mono.just(feedbackMessageContext);
        };
        return messageContext.flatMap(map -> {
            promptManager.addMessage(idSource, functionName, ROLE.USER, message, map);
            return promptManager.exchangeMessages(idSource, functionName, map, GPT3Model.GPT_3_5_TURBO, true);
        }).flatMap(chatCompletionResult -> Mono.just(chatCompletionResult.getChoices().get(0).getMessage().getContent()));
    }

    private Mono<String> getLastPromptResponseContent(S idSource, String functionName) {
        return promptManager.getContext().getPrompt(functionName).flatMap(prompt -> promptManager.getIdentifier(idSource)
                .map(identifier -> prompt.getPromptMessageContext().get(identifier))
                .map(chatMessages -> chatMessages.get(chatMessages.size() - 1).getContent()));
    }

    protected abstract String addContents(String functionName);
}
