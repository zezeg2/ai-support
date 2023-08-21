package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.common.enums.Role;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.gpt.ModelMapper;
import io.github.zezeg2.aisupport.common.util.BuildFormatUtil;
import io.github.zezeg2.aisupport.config.properties.Model;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.*;
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
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "reactive")
public abstract class ReactiveResultValidator {
    /**
     * The role of Validator for optimizing validation prompts
     */
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


    protected Mono<FeedbackMessageContext> init(PromptMessageContext promptMessageContext, String identifier) {
        return Mono.defer(() -> promptManager.getContextHolder().<FeedbackMessageContext>createMessageContext(ContextType.FEEDBACK, getNamespace(promptMessageContext.getFunctionName()), identifier)
                .flatMap(feedbackMessageContext -> {
                    Model annotatedModel = this.getClass().getAnnotation(ValidateTarget.class).model();
                    AIModel model = annotatedModel.equals(Model.NONE) ? ModelMapper.map(openAIProperties.getModel()) : ModelMapper.map(annotatedModel);
                    feedbackMessageContext.setUserInput(promptMessageContext.getUserInput());
                    feedbackMessageContext.setModel(model);
                    return buildTemplate(promptMessageContext.getFunctionName(), feedbackMessageContext)
                            .flatMap(template -> promptManager.addMessageToContext(ContextType.FEEDBACK, feedbackMessageContext, Role.SYSTEM, template))
                            .thenReturn(feedbackMessageContext);
                }));
    }

    /**
     * Builds the feedback template based on the function name and the validator's role (if provided).
     * This method returns a {@code Mono<String>} representing the asynchronous completion of the template building process.
     *
     * @param functionName           The name of the function.
     * @param feedbackMessageContext Feedback context to register for system messages
     * @return A {@code Mono<String>} representing the validate template as a string.
     */
    protected Mono<String> buildTemplate(String functionName, FeedbackMessageContext feedbackMessageContext) {
        return addTemplateContents(functionName, feedbackMessageContext)
                .flatMap(content -> getPrompt(functionName).map(Prompt::getResultFormat)
                        .flatMap(resultFormat -> Mono.just(this.role == null ? TemplateConstants.FEEDBACK_FRAME.formatted(content, resultFormat, BuildFormatUtil.getFormatString(FeedbackResponse.class)) :
                                TemplateConstants.FEEDBACK_FRAME_WITH_ROLE.formatted(this.role, content, resultFormat, BuildFormatUtil.getFormatString(FeedbackResponse.class)))));
    }


    /**
     * Validates the AI model results for the specified function, identifier, and AI model.
     * This method returns a {@code Mono<String>} representing the validated result as a string.
     *
     * @param promptMessageContext Prompt Message context for calling openai chat completion api.
     * @return A {@code Mono<String>} representing the validated result as a string.
     */
    public Mono<String> validate(PromptMessageContext promptMessageContext) {
        return ignoreCondition(promptMessageContext.getFunctionName(), promptMessageContext.getIdentifier()).flatMap(ignore -> {
            if (ignore) return getLastPromptResponseContent(promptMessageContext);
            return init(promptMessageContext, promptMessageContext.getIdentifier())
                    .flatMap(feedbackMessageContext ->
                            getLastPromptResponseContent(promptMessageContext).log(this.getClass().getSimpleName())
                                    .flatMap(lastResponseContent -> exchangeMessages(feedbackMessageContext, lastResponseContent, ContextType.FEEDBACK, feedbackMessageContext.getModel()).log(this.getClass().getSimpleName())
                                            .flatMap(lastFeedbackContent -> {
                                                FeedbackResponse feedbackResult;
                                                try {
                                                    feedbackResult = mapper.readValue(lastFeedbackContent, FeedbackResponse.class);
                                                } catch (JsonProcessingException e) {
                                                    return promptManager.getContextHolder().deleteMessagesFromLast(ContextType.FEEDBACK, feedbackMessageContext, 2).then(Mono.error(e));
                                                }
                                                if (feedbackResult.isValid()) {
                                                    promptMessageContext.getFeedbackMessageContexts().add(feedbackMessageContext);
                                                    return promptManager.getContextHolder().saveMessageContext(ContextType.PROMPT, promptMessageContext).ofType(String.class);
                                                }
                                                else
                                                    return Mono.defer(() -> exchangeMessages(promptMessageContext, lastFeedbackContent, ContextType.PROMPT, promptMessageContext.getModel())
                                                            .flatMap(r -> Mono.error(new RuntimeException("Feedback on results exists\n" + lastFeedbackContent))));
                                            }))
                                    .retry(openAIProperties.getValidateRetry() - 1)
                    ).switchIfEmpty(Mono.defer(() -> getLastPromptResponseContent(promptMessageContext)));
        });
    }

    /**
     * Exchanges messages with the AI model and retrieves the response content.
     * This method returns a {@code Mono<String>} representing the content of the AI model's response as a string.
     *
     * @param messageContext Message context for calling openai chat completion api.
     * @param message        The content of the chat message.
     * @param contextType    The type of context (prompt or feedback).
     * @param model          The AI model to use for the chat completion.
     * @return A {@code Mono<String>} representing the content of the AI model's response as a string.
     */
    protected Mono<String> exchangeMessages(MessageContext messageContext, String message, ContextType contextType, AIModel model) {
        return promptManager.addMessageToContext(contextType, messageContext, Role.USER, message)
                .then(Mono.defer(() -> {
                    Mono<Double> topPMono = contextType == ContextType.PROMPT ? promptManager.getContextHolder().get(messageContext.getFunctionName()).map(Prompt::getTopP)
                            : Mono.just(this.getClass().getAnnotation(ValidateTarget.class).topP());
                    return topPMono.flatMap(topP -> promptManager.exchangeMessages(contextType, messageContext, model, topP, true)
                            .flatMap(context -> {
                                List<ChatMessage> messages = context.getMessages();
                                return Mono.just(messages.get(messages.size() - 1).getContent());
                            }));
                }));
    }

    /**
     * Gets the content of the last prompt response from the chat context.
     * This method returns a {@code Mono<String>} representing the content of the last prompt response as a string.
     *
     * @param promptMessageContext Prompt message context for calling openai chat completion api.
     * @return A {@code Mono<String>} representing the content of the last prompt response as a string.
     */
    protected Mono<String> getLastPromptResponseContent(PromptMessageContext promptMessageContext) {
        return Mono.defer(() -> {
            List<ChatMessage> promptMessageList = promptMessageContext.getMessages();
            return Mono.just(promptMessageList.get(promptMessageList.size() - 1).getContent());
        });
    }

    /**
     * Adds the necessary template contents for feedback.
     * implement this abstract class to add validate(inspection) items
     * This method returns a {@code Mono<String>} representing the validate(inspection) items for feedback as a string.
     *
     * @param functionName           The name of the function.
     * @param feedbackMessageContext Feedback context to refer
     * @return A {@code Mono<String>} representing the template contents for feedback as a string.
     */
    protected abstract Mono<String> addTemplateContents(String functionName, FeedbackMessageContext feedbackMessageContext);

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
     * Determines whether the validation should be ignored based on prompt or message context information
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
