package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.reactive.function.ReactiveAIFunction;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ReactiveAISupport {
    private final ObjectMapper mapper;
    private final ReactivePromptManager promptManager;
    protected final ReactiveResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraintList) {
        return new ReactiveAIFunction<>(functionName, command, constraintList, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, 1d);
    }

    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraintList, double topP) {
        return new ReactiveAIFunction<>(functionName, command, constraintList, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, topP);
    }
}
