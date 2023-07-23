package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
@ValidateTarget(global = true)
public class ReactiveJsonResultValidator extends ReactiveResultValidator {
    public ReactiveJsonResultValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    @Override
    protected Mono<String> addTemplateContents(String functionName) {
        return promptManager.getContextHolder().get(functionName).map(prompt -> TemplateConstants.JSON_VALIDATE_TEMPLATE.formatted(prompt.getResultFormat()));
    }
}
