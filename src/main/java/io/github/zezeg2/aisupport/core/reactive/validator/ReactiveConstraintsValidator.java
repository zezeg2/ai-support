package io.github.zezeg2.aisupport.core.reactive.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
@ValidateTarget(global = true, order = Integer.MAX_VALUE - 1)
public class ReactiveConstraintsValidator extends ReactiveResultValidator {
    public ReactiveConstraintsValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    @Override
    protected Mono<String> addTemplateContents(String functionName) {
        return getPrompt(functionName).flatMap(prompt -> Mono.just(prompt.getConstraints()))
                .map(constraintsString -> (TemplateConstants.CONSTRAINT_VALIDATE_TEMPLATE.formatted(constraintsString)));
    }

    @Override
    protected Mono<Boolean> ignoreCondition(String functionName, String identifier) {
        return getPrompt(functionName).map(prompt -> prompt.getConstraints().isEmpty());
    }
}
