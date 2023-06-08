package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.DefaultAIFunction;
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;
import io.github.zezeg2.aisupport.core.validator.DefaultResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class DefaultAISupport {
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final DefaultPromptManager promptManager;
    protected final DefaultResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    public <T extends BaseSupportType> DefaultAIFunction<T> createFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new DefaultAIFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, promptManager, resultValidatorChain, openAIProperties);
    }
}
