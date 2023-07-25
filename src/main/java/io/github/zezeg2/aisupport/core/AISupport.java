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


/**
 * The AISupport class provides functionalities to create AI functions with specific return types, commands, and constraints.
 */
@RequiredArgsConstructor
public class AISupport {
    private final ObjectMapper mapper;
    private final PromptManager promptManager;
    protected final ResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    /**
     * Creates an AI function with the specified return type, function name, command, and constraint list.
     *
     * @param returnType     The return type of the AI function.
     * @param functionName   The name of the AI function.
     * @param command        The command associated with the AI function.
     * @param constraintList The list of constraints applied to the AI function.
     * @param <T>            The type of the return value of the AI function, extending BaseSupportType.
     * @return AIFunction instance representing the created AI function.
     */
    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraintList) {
        return new AIFunction<>(functionName, command, constraintList, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, 1d);
    }

    /**
     * Creates an AI function with the specified return type, function name, command, constraint list, and topP value.
     *
     * @param returnType     The return type of the AI function.
     * @param functionName   The name of the AI function.
     * @param command        The command associated with the AI function.
     * @param constraintList The list of constraints applied to the AI function.
     * @param topP           The topP value used for AI model validation during execution.
     * @param <T>            The type of the return value of the AI function, extending BaseSupportType.
     * @return AIFunction instance representing the created AI function.
     */
    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraintList, double topP) {
        return new AIFunction<>(functionName, command, constraintList, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, topP);
    }
}
