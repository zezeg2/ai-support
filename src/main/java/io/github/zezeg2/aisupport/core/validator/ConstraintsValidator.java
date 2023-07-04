package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
@ValidateTarget(global = true, order = Integer.MAX_VALUE - 1)
public class ConstraintsValidator extends ResultValidator {
    public ConstraintsValidator(PromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    @Override
    protected String addTemplateContents(String functionName) {
        String constraints = getPrompt(functionName).getConstraints();
        return TemplateConstants.CONSTRAINT_VALIDATE_TEMPLATE.formatted(constraints);
    }

    @Override
    protected boolean ignoreCondition(String functionName, String identifier) {
        return getPrompt(functionName).getConstraints().isEmpty();
    }
}
