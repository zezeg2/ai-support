package io.github.zezeg2.aisupport.core.function;

import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The ExecuteParameters class represents the parameters used for executing an AI function.
 * It is used to specify the identifier, list of arguments, example, and AI model for the execution.
 *
 * @param <T> The type of the return value for the AI function.
 */
@Builder
@Getter
@Setter
public class ExecuteParameters<T> {
    /**
     * The identifier for the AI function execution.
     */
    private String identifier;

    /**
     * The list of arguments for the AI function.
     */
    private List<Argument<?>> args;

    /**
     * An example object used in the AI function execution.
     */
    private T example;

    /**
     * The AI model to be used for the AI function execution.
     */
    private AIModel model;
}
