package io.github.zezeg2.aisupport.core.validator;

import java.util.Arrays;
import java.util.List;

/**
 * The ResultValidatorChain class represents a chain of ResultValidator instances that are used to validate AI model results in a chat-based AI system.
 * It allows chaining multiple ResultValidator instances together and performing validation based on specified target functions.
 */
public class ResultValidatorChain {

    /**
     * The list of ResultValidator instances forming the chain.
     */
    protected final List<ResultValidator> validators;

    /**
     * Constructs a ResultValidatorChain with the given list of validators.
     *
     * @param validators The list of ResultValidator instances to be used in the chain.
     */
    public ResultValidatorChain(List<ResultValidator> validators) {
        this.validators = validators;
    }

    /**
     * Validates the AI model results for the specified function and identifier using the validators in the chain.
     * The validation is performed based on the target function's annotations defined in the validators.
     * If a target function is marked as "global" or is explicitly listed in the validator's target names,
     * the corresponding validator is used to validate the results.
     *
     * @param functionName The name of the function to validate.
     * @param identifier   The identifier of the chat context.
     * @param target       The target result to validate.
     * @return The validated result as a string.
     */
    public String validate(String functionName, String identifier, String target) {
        String result = target;
        for (ResultValidator validator : validators) {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            if (targetFunction.global() || targetFunctionList.contains(functionName)) {
                result = validator.validate(functionName, identifier);
            }
        }
        return result;
    }

    /**
     * Retrieves the list of ResultValidator instances that are applicable to the specified function name.
     * The applicable validators are filtered based on the target function's annotations defined in the validators.
     * If a target function is marked as "global" or is explicitly listed in the validator's target names,
     * the corresponding validator is included in the returned list.
     *
     * @param functionName The name of the function for which to retrieve applicable validators.
     * @return The list of applicable ResultValidator instances.
     */
    public List<ResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
