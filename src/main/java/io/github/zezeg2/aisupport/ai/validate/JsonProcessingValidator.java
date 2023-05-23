package io.github.zezeg2.aisupport.ai.validate;

import com.theokanning.openai.service.OpenAiService;

public class JsonProcessingValidator extends ResultValidator {

    public JsonProcessingValidator(OpenAiService service, String promptKey) {
        super(service, promptKey);
    }

    @Override
    public String validate(String target) {
        return null;
    }
}
