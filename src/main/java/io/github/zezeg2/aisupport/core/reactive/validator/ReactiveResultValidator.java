package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.config.properties.MODEL;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
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

    protected Mono<Void> init(String functionName, String identifier) {
        return Mono.just(identifier)
                .flatMap(id -> promptManager.getContext().getFeedbackChatMessages(getNamespace(functionName), id)
                        .flatMap(feedbackChatMessages -> {
                            if (feedbackChatMessages.getContent().isEmpty()) {
                                return buildTemplate(functionName)
                                        .flatMap(template -> promptManager.addMessage(getNamespace(functionName), identifier, ROLE.SYSTEM, template, ContextType.FEEDBACK));
                            }
                            return Mono.empty();
                        }));
    }

    private Mono<String> buildTemplate(String functionName) {
        return addTemplateContents(functionName).map(content -> TemplateConstants.FEEDBACK_FRAME.formatted(BuildFormatUtil.getFormatString(FeedbackResponse.class), content));
    }

    public Mono<String> validate(String functionName, String identifier) {
        MODEL annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
        AIModel model = annotatedModel.equals(MODEL.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
        return validate(functionName, identifier, model);
    }

    public Mono<String> validate(String functionName, String identifier, AIModel model) {
        return init(functionName, identifier)
                .then(Mono.defer(() -> getLastPromptResponseContent(functionName, identifier))
                        .flatMap(lastResponseContent -> Mono.defer(() -> exchangeMessages(functionName, identifier, lastResponseContent, ContextType.FEEDBACK, model))
                                .flatMap(lastFeedbackContent -> {
                                    FeedbackResponse feedbackResult;
                                    try {
                                        feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                    } catch (JsonProcessingException e) {
                                        return promptManager.getContext().deleteLastFeedbackMessage(getNamespace(functionName), identifier, 2)
                                                .then(exchangeMessages(functionName, identifier, lastResponseContent, ContextType.FEEDBACK, model)
                                                        .flatMap(ignored -> Mono.<String>error(new RuntimeException(e))));
                                    }

                                    if (feedbackResult.isValid()) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.defer(() -> {
                                            Mono<String> result = exchangeMessages(functionName, identifier, lastFeedbackContent, ContextType.PROMPT, model);
                                            return result.flatMap(r -> Mono.error(new RuntimeException("Feedback on results exists\n" + lastFeedbackContent)));
                                        });
                                    }
                                })
                                .retry(MAX_ATTEMPTS - 1)
                                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(functionName, identifier)))));
    }


    protected Mono<String> exchangeMessages(String functionName, String identifier, String message, ContextType contextType, AIModel model) {
        return promptManager.addMessage(identifier, contextType.equals(ContextType.PROMPT) ? functionName : getNamespace(functionName), ROLE.USER, message, contextType)
                .then(switch (contextType) {
                    case PROMPT -> promptManager.getContext().get(functionName)
                            .map(Prompt::getTopP)
                            .flatMap(topP -> promptManager.exchangePromptMessages(functionName, identifier, model, topP, true)
                                    .map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent()));
                    case FEEDBACK -> {
                        double topP = this.getClass().getAnnotation(ValidateTarget.class).topP();
                        yield promptManager.exchangeFeedbackMessages(getNamespace(functionName), identifier, model, topP, true)
                                .map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                    }
                });
    }

    protected Mono<String> getLastPromptResponseContent(String functionName, String identifier) {
        return Mono.just(identifier)
                .flatMap(id -> promptManager.getContext().getPromptChatMessages(functionName, id))
                .flatMap(promptMessageList -> Mono.just(promptMessageList.getContent().get(promptMessageList.getContent().size() - 1).getContent()));
    }

    protected abstract Mono<String> addTemplateContents(String functionName);

    protected Mono<Prompt> getPrompt(String functionName) {
        return promptManager.getContext().get(functionName);
    }
}
