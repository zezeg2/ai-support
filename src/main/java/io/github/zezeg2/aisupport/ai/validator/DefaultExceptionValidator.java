package io.github.zezeg2.aisupport.ai.validator;

import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.context.PromptContext;

public class DefaultExceptionValidator extends ExceptionValidator {
    protected final String FIX_EXCEPTION_TEMPLATE = """
            Fix a previous response that causes an following Exception
            ```
            Exception : %s
            Message : %s
            ```
            and Remind Constraints and Result Format
            Constraints
            - Only respond with your `return` value. Do not include any other explanatory text in your response.
            %s
            Result Format : %s
            """;

    public DefaultExceptionValidator(OpenAiService service, PromptContext context, Exception exception) {
        super(service, context, exception);
    }

    @Override
    public boolean isRequired(String target) {
        return false;
    }

    @Override
    public String validate(String target) {
        return "";
    }
}
