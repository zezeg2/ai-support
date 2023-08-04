package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessageContext;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * ReactiveConstraintsValidator is a component class that extends the abstract ReactiveResultValidator, providing validation
 * for AI model results with constraints in a reactive chat-based AI system.
 * <p>
 * This class is marked as a component and will be automatically detected and registered in the application context.
 * It is conditionally enabled based on the property "ai-supporter.context.environment" with the value "reactive".
 * Additionally, it is annotated with @ValidateTarget(global = true, order = Integer.MAX_VALUE - 1) to indicate that this
 * validator is applicable globally for validation, and it has a higher priority in the validation order compared to others.
 */
@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "reactive")
@ValidateTarget(global = true, order = Integer.MAX_VALUE - 1)
public class ReactiveConstraintsValidator extends ReactiveResultValidator {

    /**
     * Constructs a ReactiveConstraintsValidator with the necessary dependencies.
     *
     * @param promptManager    The ReactivePromptManager instance for managing prompts and messages in a reactive context.
     * @param mapper           The ObjectMapper for JSON serialization and deserialization.
     * @param openAIProperties The properties for configuring the OpenAI service.
     */
    public ReactiveConstraintsValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    /**
     * Adds the necessary template contents for constraints validation feedback in a reactive manner.
     *
     * @param functionName           The name of the function.
     * @param feedbackMessageContext Feedback context to refer
     * @return A Mono emitting the template contents for constraints validation feedback as a string.
     */
    @Override
    protected Mono<String> addTemplateContents(String functionName, FeedbackMessageContext feedbackMessageContext) {
        return getPrompt(functionName)
                .flatMap(prompt -> Mono.just(prompt.getConstraints()))
                .map(constraintsString -> (TemplateConstants.CONSTRAINT_VALIDATE_TEMPLATE.formatted(constraintsString)));
    }

    /**
     * Determines whether the validation should be ignored based on the function and identifier in a reactive manner.
     * If the constraints for the function are empty, the validation will be ignored.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return A Mono emitting true if the validation should be ignored, false otherwise.
     */
    @Override
    protected Mono<Boolean> ignoreCondition(String functionName, String identifier) {
        return getPrompt(functionName)
                .map(prompt -> prompt.getConstraints().isEmpty());
    }
}

