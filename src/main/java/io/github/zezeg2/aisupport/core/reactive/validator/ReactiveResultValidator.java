package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.common.util.BuildFormatUtil;
import io.github.zezeg2.aisupport.config.properties.MODEL;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The abstract ReactiveResultValidator class provides a framework for validating AI model results in a chat-based AI system in a reactive manner.
 */
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
public abstract class ReactiveResultValidator {
    protected final int MAX_ATTEMPTS = 3;
    @Setter
    protected String role = null;
    protected final ReactivePromptManager promptManager;
    protected final ObjectMapper mapper;
    protected final OpenAIProperties openAIProperties;

    /**
     * Constructs a ReactiveResultValidator with the necessary dependencies.
     *
     * @param promptManager    The ReactivePromptManager instance for managing prompts and messages.
     * @param mapper           The ObjectMapper for JSON serialization and deserialization.
     * @param openAIProperties The properties for configuring the OpenAI service.
     */
    public ReactiveResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        this.promptManager = promptManager;
        this.mapper = mapper;
        this.openAIProperties = openAIProperties;
    }

    /**
     * Gets the namespace for the prompt context based on the function name and the validator's class name.
     *
     * @param functionName The name of the function.
     * @return The namespace for the prompt context.
     */
    protected String getNamespace(String functionName) {
        return String.join(":", List.of(functionName, this.getClass().getSimpleName()));
    }

    /**
     * Initializes the feedback context with a system message containing the feedback template.
     * This method returns a {@code Mono<Void>} representing the asynchronous completion of the initialization process.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return A {@code Mono<Void>} representing the completion of the initialization process.
     */
    protected Mono<Void> init(String functionName, String identifier) {
        return Mono.defer(() -> promptManager.getContextHolder().getContext(ContextType.FEEDBACK, getNamespace(functionName), identifier)
                .flatMap(context -> {
                    if (context.getMessages().isEmpty()) {
                        return buildTemplate(functionName)
                                .flatMap(template -> promptManager.addMessageToContext(getNamespace(functionName), identifier, ROLE.SYSTEM, template, ContextType.FEEDBACK));
                    }
                    return Mono.empty();
                }));
    }

    /**
     * Builds the feedback template based on the function name and the validator's role (if provided).
     * This method returns a {@code Mono<String>} representing the asynchronous completion of the template building process.
     *
     * @param functionName The name of the function.
     * @return A {@code Mono<String>} representing the feedback template as a string.
     */
    private Mono<String> buildTemplate(String functionName) {
        return addTemplateContents(functionName).flatMap(content -> getPrompt(functionName).map(Prompt::getResultFormat).flatMap(resultFormat -> Mono.just(this.role == null ? TemplateConstants.FEEDBACK_FRAME.formatted(content, resultFormat, BuildFormatUtil.getFormatString(FeedbackResponse.class)) :
                TemplateConstants.FEEDBACK_FRAME_WITH_ROLE.formatted(this.role, content, resultFormat, BuildFormatUtil.getFormatString(FeedbackResponse.class)))));
    }

    /**
     * Validates the AI model results for the specified function and identifier.
     * This method returns a {@code Mono<String>} representing the validated result as a string.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return A {@code Mono<String>} representing the validated result as a string.
     */
    public Mono<String> validate(String functionName, String identifier) {
        MODEL annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
        AIModel model = annotatedModel.equals(MODEL.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
        return ignoreCondition(functionName, identifier).flatMap(ignore -> {
            if (!ignore) return validate(functionName, identifier, model);
            return getLastPromptResponseContent(functionName, identifier);
        });
    }

    /**
     * Validates the AI model results for the specified function, identifier, and AI model.
     * This method returns a {@code Mono<String>} representing the validated result as a string.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @param model        The AI model to use for validation.
     * @return A {@code Mono<String>} representing the validated result as a string.
     */
    public Mono<String> validate(String functionName, String identifier, AIModel model) {
        return init(functionName, identifier)
                .then(Mono.defer(() ->
                        getLastPromptResponseContent(functionName, identifier).log()
                                .flatMap(lastResponseContent -> exchangeMessages(functionName, identifier, lastResponseContent, ContextType.FEEDBACK, model).log()
                                        .flatMap(lastFeedbackContent -> {
                                            FeedbackResponse feedbackResult;
                                            try {
                                                feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                            } catch (JsonProcessingException e) {
                                                return promptManager.getContextHolder().deleteMessagesFromLast(ContextType.FEEDBACK, getNamespace(functionName), identifier, 2)
                                                        .then(exchangeMessages(functionName, identifier, lastResponseContent, ContextType.FEEDBACK, model)
                                                                .flatMap(ignored -> Mono.<String>error(new RuntimeException(e))));
                                            }
                                            if (feedbackResult.isValid()) return Mono.empty();
                                            else return Mono.defer(() -> {
                                                Mono<String> result = exchangeMessages(functionName, identifier, lastFeedbackContent, ContextType.PROMPT, model);
                                                return result.flatMap(r -> Mono.error(new RuntimeException("Feedback on results exists\n" + lastFeedbackContent)));
                                            });
                                        }))
                                .retry(MAX_ATTEMPTS - 1)
                ))
                .switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(functionName, identifier)));
    }

    /**
     * Exchanges messages with the AI model and retrieves the response content.
     * This method returns a {@code Mono<String>} representing the content of the AI model's response as a string.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @param message      The content of the chat message.
     * @param contextType  The type of context (prompt or feedback).
     * @param model        The AI model to use for the chat completion.
     * @return A {@code Mono<String>} representing the content of the AI model's response as a string.
     */
    protected Mono<String> exchangeMessages(String functionName, String identifier, String message, ContextType contextType, AIModel model) {
        return promptManager.addMessageToContext(contextType.equals(ContextType.PROMPT) ? functionName : getNamespace(functionName), identifier, ROLE.USER, message, contextType)
                .then(switch (contextType) {
                    case PROMPT -> promptManager.getContextHolder().get(functionName)
                            .map(Prompt::getTopP)
                            .flatMap(topP -> promptManager.exchangeMessages(contextType, functionName, identifier, model, topP, true)
                                    .map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent()));
                    case FEEDBACK ->
                            promptManager.exchangeMessages(contextType, getNamespace(functionName), identifier, model, this.getClass().getAnnotation(ValidateTarget.class).topP(), true)
                                    .map(chatCompletionResult -> chatCompletionResult.getChoices().get(0).getMessage().getContent());
                });
    }

    /**
     * Gets the content of the last prompt response from the chat context.
     * This method returns a {@code Mono<String>} representing the content of the last prompt response as a string.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return A {@code Mono<String>} representing the content of the last prompt response as a string.
     */
    protected Mono<String> getLastPromptResponseContent(String functionName, String identifier) {
        return Mono.defer(() -> promptManager.getContextHolder().getContext(ContextType.PROMPT, functionName, identifier)
                .flatMap(context -> Mono.just(context.getMessages().get(context.getMessages().size() - 1).getContent())));
    }

    /**
     * Adds the necessary template contents for feedback.
     * This method returns a {@code Mono<String>} representing the template contents for feedback as a string.
     *
     * @param functionName The name of the function.
     * @return A {@code Mono<String>} representing the template contents for feedback as a string.
     */
    protected abstract Mono<String> addTemplateContents(String functionName);

    /**
     * Gets the prompt for the specified function.
     * This method returns a {@code Mono<Prompt>} representing the prompt associated with the function.
     *
     * @param functionName The name of the function.
     * @return A {@code Mono<Prompt>} representing the prompt associated with the function.
     */
    protected Mono<Prompt> getPrompt(String functionName) {
        return promptManager.getContextHolder().get(functionName);
    }

    /**
     * Determines whether the validation should be ignored based on the function and identifier.
     * This method returns a {@code Mono<Boolean>} representing whether the validation should be ignored (true) or not (false).
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return A {@code Mono<Boolean>} representing whether the validation should be ignored (true) or not (false).
     */
    protected Mono<Boolean> ignoreCondition(String functionName, String identifier) {
        return Mono.just(false);
    }
}
