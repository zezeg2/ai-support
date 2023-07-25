package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ConstraintsValidator is a component class that extends the abstract ResultValidator, providing validation for AI model
 * results with constraints in a chat-based AI system.
 * <p>
 * This class is marked as a component and will be automatically detected and registered in the application context.
 * It is conditionally enabled based on the property "ai-supporter.context.environment" with the value "synchronous".
 * Additionally, it is annotated with @ValidateTarget(global = true, order = Integer.MAX_VALUE - 1) to indicate that this
 * validator is applicable globally for validation and it has a higher priority in the validation order compared to others.
 */
@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
@ValidateTarget(global = true, order = Integer.MAX_VALUE - 1)
public class ConstraintsValidator extends ResultValidator {

    /**
     * Constructs a ConstraintsValidator with the necessary dependencies.
     *
     * @param promptManager    The PromptManager instance for managing prompts and messages.
     * @param mapper           The ObjectMapper for JSON serialization and deserialization.
     * @param openAIProperties The properties for configuring the OpenAI service.
     */
    public ConstraintsValidator(PromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    /**
     * Adds the necessary template contents for constraints validation feedback.
     *
     * @param functionName The name of the function.
     * @return The template contents for constraints validation feedback as a string.
     */
    @Override
    protected String addTemplateContents(String functionName) {
        String constraints = getPrompt(functionName).getConstraints();
        return TemplateConstants.CONSTRAINT_VALIDATE_TEMPLATE.formatted(constraints);
    }

    /**
     * Determines whether the validation should be ignored based on the function and identifier.
     * If the constraints for the function are empty, the validation will be ignored.
     *
     * @param functionName The name of the function.
     * @param identifier   The identifier of the chat context.
     * @return true if the validation should be ignored, false otherwise.
     */
    @Override
    protected boolean ignoreCondition(String functionName, String identifier) {
        return getPrompt(functionName).getConstraints().isEmpty();
    }
}
