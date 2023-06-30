package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.AIFunction;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.core.validator.ResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class AISupport {
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final PromptManager promptManager;
    protected final ResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String purpose, List<Constraint> constraintList) {
        return new AIFunction<>(functionName, purpose, constraintList, returnType, mapper, resolver, promptManager, resultValidatorChain, openAIProperties);
    }
}
