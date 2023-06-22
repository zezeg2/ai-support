package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
@ValidateTarget(global = true)
public class JsonResultValidator extends ResultValidator {
    public JsonResultValidator(PromptManager promptManager, ObjectMapper mapper) {
        super(promptManager, mapper);
    }

    @Override
    protected String addContents(String functionName) {
        String resultFormat = promptManager.getContext().get(functionName).getResultFormat();
        return TemplateConstants.JSON_VALIDATE_TEMPLATE.formatted(resultFormat);
    }
}
