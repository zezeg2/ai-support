package io.github.zezeg2.aisupport.ai.function.prompt.refactor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
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

    public <T> ReactiveFunction<T> createFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new ReactiveFunction<>(functionName, purpose, constraintList, (Class<Mono<T>>) (Class<?>) Mono.class, service, mapper, resolver, promptManager, resultValidatorChain, openAIProperties, returnType);
    }
}
