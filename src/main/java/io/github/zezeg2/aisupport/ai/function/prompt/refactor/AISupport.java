package io.github.zezeg2.aisupport.ai.function.prompt.refactor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class AISupport {
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final DefaultPromptManager promptManager;
    protected final DefaultResultValidatorChain resultValidatorChain;
    protected final ExceptionValidator exceptionValidator;
    private final OpenAIProperties openAIProperties;

    public <T> DefaultFunction<T> createFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new DefaultFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }
}
