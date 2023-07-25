package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.function.AIFunction;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.core.validator.ResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class AISupport {
    private final ObjectMapper mapper;
    private final PromptManager promptManager;
    protected final ResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraintList) {
        return new AIFunction<>(functionName, command, constraintList, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, 1d);
    }

    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraintList, double topP) {
        return new AIFunction<>(functionName, command, constraintList, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, topP);
    }
}
