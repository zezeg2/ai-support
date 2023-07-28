package io.github.zezeg2.aisupport.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.core.reactive.function.ReactiveAIFunction;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import lombok.RequiredArgsConstructor;

import java.util.List;


/**
 * The ReactiveAISupport class provides functionalities to create reactive AI functions with specific return types, commands, and constraints.
 * The functions created using this class perform non-blocking operations in the event loop environment and return reactive streams.
 */
@RequiredArgsConstructor
public class ReactiveAISupport {
    private final ObjectMapper mapper;
    private final ReactivePromptManager promptManager;
    protected final ReactiveResultValidatorChain resultValidatorChain;
    private final OpenAIProperties openAIProperties;

    /**
     * Creates a reactive AI function with the specified return type, function name, command, and constraint list.
     *
     * @param returnType   The return type of the reactive AI function.
     * @param functionName The name of the reactive AI function.
     * @param command      The command associated with the reactive AI function.
     * @param constraints  The list of constraints applied to the reactive AI function.
     * @param <T>          The type of the return value of the reactive AI function, extending BaseSupportType.
     * @return ReactiveAIFunction instance representing the created reactive AI function.
     */
    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraints) {
        return new ReactiveAIFunction<>(functionName, null, command, constraints, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, 1d);
    }

    /**
     * Creates a reactive AI function with the specified return type, function name, command, and constraint list.
     *
     * @param returnType   The return type of the reactive AI function.
     * @param functionName The name of the reactive AI function.
     * @param command      The command associated with the reactive AI function.
     * @param constraints  The list of constraints applied to the reactive AI function.
     * @param topP         The topP value used for AI model validation during execution.
     * @param <T>          The type of the return value of the reactive AI function, extending BaseSupportType.
     * @return ReactiveAIFunction instance representing the created reactive AI function.
     */
    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String command, List<Constraint> constraints, double topP) {
        return new ReactiveAIFunction<>(functionName, null, command, constraints, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, topP);
    }

    /**
     * Creates a reactive AI function with the specified return type, function name, command, constraint list, and topP value.
     *
     * @param returnType   The return type of the reactive AI function.
     * @param functionName The name of the reactive AI function.
     * @param command      The command associated with the reactive AI function.
     * @param constraints  The list of constraints applied to the reactive AI function.
     * @param <T>          The type of the return value of the reactive AI function, extending BaseSupportType.
     * @return ReactiveAIFunction instance representing the created reactive AI function.
     */
    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String role, String command, List<Constraint> constraints) {
        return new ReactiveAIFunction<>(functionName, role, command, constraints, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, 1d);
    }

    /**
     * Creates a reactive AI function with the specified return type, function name, command, constraint list, and topP value.
     *
     * @param returnType   The return type of the reactive AI function.
     * @param functionName The name of the reactive AI function.
     * @param command      The command associated with the reactive AI function.
     * @param constraints  The list of constraints applied to the reactive AI function.
     * @param topP         The topP value used for AI model validation during execution.
     * @param <T>          The type of the return value of the reactive AI function, extending BaseSupportType.
     * @return ReactiveAIFunction instance representing the created reactive AI function.
     */
    public <T extends BaseSupportType> ReactiveAIFunction<T> createFunction(Class<T> returnType, String functionName, String role, String command, List<Constraint> constraints, double topP) {
        return new ReactiveAIFunction<>(functionName, role, command, constraints, returnType, mapper, promptManager, resultValidatorChain, openAIProperties, topP);
    }
}

