package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * ReactiveJsonResultValidator is a component class that extends the abstract ReactiveResultValidator, providing specific
 * validation for JSON-based AI model results in a reactive chat-based AI system.
 * <p>
 * This class is marked as a component and will be automatically detected and registered in the application context.
 * It is conditionally enabled based on the property "ai-supporter.context.environment" with the value "eventloop".
 * Additionally, it is annotated with @ValidateTarget(global = true) to indicate that this validator is applicable globally
 * for validation.
 */
@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
@ValidateTarget(global = true)
public class ReactiveJsonResultValidator extends ReactiveResultValidator {

    /**
     * Constructs a ReactiveJsonResultValidator with the necessary dependencies.
     *
     * @param promptManager    The ReactivePromptManager instance for managing prompts and messages in a reactive context.
     * @param mapper           The ObjectMapper for JSON serialization and deserialization.
     * @param openAIProperties The properties for configuring the OpenAI service.
     */
    public ReactiveJsonResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    /**
     * Adds the necessary template contents for JSON validation feedback in a reactive manner.
     *
     * @param functionName The name of the function.
     * @return A Mono emitting the template contents for JSON validation feedback as a string.
     */
    @Override
    protected Mono<String> addTemplateContents(String functionName) {
        return promptManager.getContextHolder().get(functionName)
                .map(prompt -> TemplateConstants.JSON_VALIDATE_TEMPLATE.formatted(prompt.getResultFormat()));
    }
}
