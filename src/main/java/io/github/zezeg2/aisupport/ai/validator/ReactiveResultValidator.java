package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.ContextType;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactiveSessionContextPromptManager;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ReactiveResultValidator implements ReactiveValidatable {
    protected final ReactiveSessionContextPromptManager promptManager;
    protected final ObjectMapper mapper;
    protected final Map<String, List<ChatMessage>> feedbackMessageContext;
    private static final int MAX_ATTEMPTS = 3;

    public ReactiveResultValidator(ReactiveSessionContextPromptManager promptManager, ObjectMapper mapper) {
        this.promptManager = promptManager;
        this.mapper = mapper;
        this.feedbackMessageContext = new ConcurrentHashMap<>();
    }

    public void initFeedbackMessageContext(ServerWebExchange exchange, String functionName) {
        promptManager.initMessageContext(exchange, functionName, buildTemplate(functionName), feedbackMessageContext);
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
    public Flux<String> validate(ServerWebExchange exchange, String functionName) {
        initFeedbackMessageContext(exchange, functionName);
        return Flux.defer(() -> getLastPromptResponseContent(exchange, functionName))
                .expand(lastPromptMessage -> Mono.defer(() -> getResponseContent(exchange, functionName, lastPromptMessage, ContextType.FEEDBACK))
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
                        .switchIfEmpty(Mono.defer(() -> getResponseContent(exchange, functionName, lastPromptMessage, ContextType.PROMPT)))
                        .repeat(MAX_ATTEMPTS - 1))
                .doOnDiscard(String.class, (message) -> {
                    throw new RuntimeException("Maximum Validate count over");
                });
    }


    private Mono<String> getResponseContent(ServerWebExchange exchange, String functionName, String message, ContextType contextType) {
        Mono<Map<String, List<ChatMessage>>> messageContext = switch (contextType) {
            case PROMPT ->
                    promptManager.getContext().getPrompt(functionName).map(ReactivePrompt::getPromptMessageContext);
            case FEEDBACK -> Mono.just(feedbackMessageContext);
        };
        return messageContext.flatMap(map -> {
            promptManager.addMessage(exchange, functionName, ROLE.USER, message, map);
            return promptManager.exchangeMessages(exchange, functionName, map, GPT3Model.GPT_3_5_TURBO, true);
        }).flatMap(chatCompletionResult -> Mono.just(chatCompletionResult.getChoices().get(0).getMessage().getContent()));
    }

    private Mono<String> getLastPromptResponseContent(ServerWebExchange exchange, String functionName) {
        return promptManager.getContext().getPrompt(functionName).map(prompt -> {
            List<ChatMessage> chatMessages = prompt.getPromptMessageContext().get(promptManager.getIdentifier(exchange));
            return chatMessages.get(chatMessages.size() - 1).getContent();
        });
    }

    protected abstract String addContents(String functionName);
}
