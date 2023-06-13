package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;
import org.springframework.stereotype.Component;

@Component
@ValidateTarget(global = true)
public class JsonResultValidator extends DefaultResultValidator {
    public JsonResultValidator(DefaultPromptManager promptManager, ObjectMapper mapper) {
        super(promptManager, mapper);
    }

    @Override
    protected String addContents(String functionName) {
        String resultFormat = promptManager.getContext().get(functionName).getResultFormat();
        return TemplateConstants.JSON_VALIDATE_TEMPLATE.formatted(resultFormat);
    }
}
