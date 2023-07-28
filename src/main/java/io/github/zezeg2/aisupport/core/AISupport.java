package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
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
    private final ConstructResolver resolver;
    private final OpenAIProperties openAIProperties;

    /**
     * Creates an AI function with the specified return type, function name, command, and constraint list.
     *
     * @param returnType   The return type of the AI function.
     * @param functionName The name of the AI function.
     * @param command      The command associated with the AI function.
     * @param constraints  The list of constraints applied to the AI function.
     * @param <T>          The type of the return value of the AI function, extending BaseSupportType.
     * @return AIFunction instance representing the created AI function.
     */
    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraints) {
        return new AIFunction<>(functionName, null, command, constraints, returnType, 1d, mapper, promptManager, resultValidatorChain, resolver, openAIProperties);
    }

    /**
     * Creates an AI function with the specified return type, function name, command, constraint list, and topP value.
     *
     * @param returnType   The return type of the AI function.
     * @param functionName The name of the AI function.
     * @param command      The command associated with the AI function.
     * @param constraints  The list of constraints applied to the AI function.
     * @param topP         The topP value used for AI model validation during execution.
     * @param <T>          The type of the return value of the AI function, extending BaseSupportType.
     * @return AIFunction instance representing the created AI function.
     */
    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraints, double topP) {
        return new AIFunction<>(functionName, null, command, constraints, returnType, topP, mapper, promptManager, resultValidatorChain, resolver, openAIProperties);
    }

    /**
     * Creates an AI function with the specified return type, function name, role, command, and constraint list.
     *
     * @param returnType   The return type of the AI function.
     * @param functionName The name of the AI function.
     * @param role         The role associated with the AI function.
     * @param command      The command associated with the AI function.
     * @param constraints  The list of constraints applied to the AI function.
     * @param <T>          The type of the return value of the AI function, extending BaseSupportType.
     * @return AIFunction instance representing the created AI function.
     */
    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String role, String command, List<Constraint> constraints) {
        return new AIFunction<>(functionName, role, command, constraints, returnType, 1d, mapper, promptManager, resultValidatorChain, resolver, openAIProperties);
    }

    /**
     * Creates an AI function with the specified return type, function name, command, constraint list, and topP value.
     *
     * @param returnType   The return type of the AI function.
     * @param functionName The name of the AI function.
     * @param command      The command associated with the AI function.
     * @param constraints  The list of constraints applied to the AI function.
     * @param topP         The topP value used for AI model validation during execution.
     * @param <T>          The type of the return value of the AI function, extending BaseSupportType.
     * @return AIFunction instance representing the created AI function.
     */
    public <T extends BaseSupportType> AIFunction<T> createFunction(Class<T> returnType, String functionName, String role, String command, List<Constraint> constraints, double topP) {
        return new AIFunction<>(functionName, role, command, constraints, returnType, topP, mapper, promptManager, resultValidatorChain, resolver, openAIProperties);
    }


}
