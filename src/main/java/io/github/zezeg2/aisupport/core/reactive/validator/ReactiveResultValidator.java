package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.core.publisher.Mono;

import java.util.List;

@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
public abstract class ReactiveResultValidator {
    protected static final int MAX_ATTEMPTS = 3;
    protected final ReactivePromptManager promptManager;
    protected final ObjectMapper mapper;
    protected final OpenAIProperties openAIProperties;

    public ReactiveResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        this.promptManager = promptManager;
        this.mapper = mapper;
        this.openAIProperties = openAIProperties;
    }

    protected String getNamespace(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName()));
    }

    protected Mono<Void> init(String identifier, String functionName) {
        return Mono.just(identifier)
                .flatMap(id -> promptManager.getContext().getFeedbackChatMessages(getNamespace(functionName), id)
                        .flatMap(feedbackChatMessages -> {
                            if (feedbackChatMessages.getContent().isEmpty()) {
                                return buildTemplate(functionName)
                                        .flatMap(template -> promptManager.addMessage(identifier, getNamespace(functionName), ROLE.SYSTEM, template, ContextType.FEEDBACK));
                            }
                            return Mono.empty();
                        }));
    }

    private Mono<String> buildTemplate(String functionName) {
        return addTemplateContents(functionName).map(content -> TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), content));
    }

    public Mono<String> validate(String identifier, String functionName) {
        AIModel model = ModelMapper.map(openAIProperties.getModel());
        return validate(identifier, functionName, model);
    }

    public Mono<String> validate(String identifier, String functionName, AIModel model) {
        return init(identifier, functionName)
                .then(Mono.defer(() -> getLastPromptResponseContent(identifier, functionName))
                        .flatMap(lastResponseContent -> Mono.defer(() -> exchangeMessages(identifier, functionName, lastResponseContent, ContextType.FEEDBACK, model))
                                .flatMap(lastFeedbackContent -> {
                                    FeedbackResponse feedbackResult;
                                    try {
                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                    } catch (JsonProcessingException e) {
                                        return promptManager.getContext().deleteLastFeedbackMessage(getNamespace(functionName), identifier, 2)
                                                .then(exchangeMessages(identifier, functionName, lastResponseContent, ContextType.FEEDBACK, model).doOnNext(ignored -> Mono.error(new RuntimeException(e))));
                                    }

                                    if (feedbackResult.isValid()) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.defer(() -> {
                                            Mono<String> result = exchangeMessages(identifier, functionName, lastFeedbackContent, ContextType.PROMPT, model);
                                            return result.flatMap(r -> Mono.error(new RuntimeException("Feedback on results exists\n" + lastFeedbackContent)));
                                        });
                                    }
                                })
                                .retry(MAX_ATTEMPTS - 1)
                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(identifier, functionName)))));
    }


    protected Mono<String> exchangeMessages(String identifier, String functionName, String message, ContextType contextType, AIModel model) {
        return promptManager.addMessage(identifier, contextType.equals(ContextType.PROMPT) ? functionName : getNamespace(functionName), ROLE.USER, message, contextType)
                .then(switch (contextType) {
                    case PROMPT ->
                            promptManager.exchangePromptMessages(identifier, functionName, GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                    case FEEDBACK ->
                            promptManager.exchangeFeedbackMessages(identifier, getNamespace(functionName), GPT3Model.GPT_3_5_TURBO, true).map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                });
    }

    protected Mono<String> getLastPromptResponseContent(String identifier, String functionName) {
        return Mono.just(identifier)
                .flatMap(id -> promptManager.getContext().getPromptChatMessages(functionName, id))
                .flatMap(promptMessageList -> Mono.just(promptMessageList.getContent().get(promptMessageList.getContent().size() - 1).getContent()));
    }

    protected abstract Mono<String> addTemplateContents(String functionName);

    protected Mono<Prompt> getPrompt(String functionName) {
        return promptManager.getContext().get(functionName);
    }
}
