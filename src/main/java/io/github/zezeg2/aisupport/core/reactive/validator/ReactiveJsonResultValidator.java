package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
@ValidateTarget(global = true)
public class ReactiveJsonResultValidator extends ReactiveResultValidator {
    public ReactiveJsonResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper) {
        super(promptManager, mapper);
    }

    @Override
    protected Mono<String> addContents(String functionName) {
        return promptManager.getContext().get(functionName).map(prompt -> TemplateConstants.JSON_VALIDATE_TEMPLATE.formatted(prompt.getResultFormat()));
    }
}
