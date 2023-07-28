package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * JsonResultValidator is a component class that extends the abstract ResultValidator, providing specific validation
 * for JSON-based AI model results in a chat-based AI system.
 * <p>
 * This class is marked as a component and will be automatically detected and registered in the application context.
 * It is conditionally enabled based on the property "ai-supporter.context.environment" with the value "synchronous".
 * Additionally, it is annotated with @ValidateTarget(global = true) to indicate that this validator is applicable
 * globally for validation.
 */
@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
@ValidateTarget(global = true)
public class JsonResultValidator extends ResultValidator {

    /**
     * Constructs a JsonResultValidator with the necessary dependencies.
     *
     * @param promptManager    The PromptManager instance for managing prompts and messages.
     * @param mapper           The ObjectMapper for JSON serialization and deserialization.
     * @param openAIProperties The properties for configuring the OpenAI service.
     */
    public JsonResultValidator(PromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    /**
     * Adds the necessary template contents for JSON validation feedback.
     *
     * @param functionName The name of the function.
     * @return The template contents for JSON validation feedback as a string.
     */


    @Override
    protected String buildTemplate(String functionName) {
        Prompt prompt = promptManager.getContextHolder().get(functionName);
        String requiredFormat = prompt.getResultFormat();
        String structureInfo = prompt.getClassStructureInfo();
        return TemplateConstants.JSON_VALIDATE_TEMPLATE.formatted(requiredFormat, structureInfo);
    }

    @Override
    protected String addTemplateContents(String functionName) {
        return null;
    }
}
