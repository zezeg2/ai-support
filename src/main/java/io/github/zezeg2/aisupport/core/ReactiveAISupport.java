package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.reactive.function.ReactiveAIFunction;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;


@RequiredArgsConstructor
public class ReactiveAISupport {
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final ReactivePromptManager promptManager;
    protected final ReactiveResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new ReactiveAIFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, openAIProperties);
    }
}
