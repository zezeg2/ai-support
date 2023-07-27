package io.github.zezeg2.aisupport.core.reactive.validator;

import io.github.zezeg2.aisupport.core.function.prompt.PromptMessageContext;
import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ReactiveResultValidatorChain class represents a chain of ReactiveResultValidator instances that are used to validate AI model results in a chat-based AI system using reactive programming.
 * It allows chaining multiple ReactiveResultValidator instances together and performing validation based on specified target functions.
 */
public class ReactiveResultValidatorChain {

    /**
     * The list of ReactiveResultValidator instances forming the chain.
     */
    protected final List<ReactiveResultValidator> validators;

    /**
     * Constructs a ReactiveResultValidatorChain with the given list of validators.
     * The validators are sorted based on the "order" value specified in their ValidateTarget annotations.
     *
     * @param validators The list of ReactiveResultValidator instances to be used in the chain.
     */
    public ReactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        this.validators = validators.stream()
                .sorted(Comparator.comparingInt(v -> v.getClass().getAnnotation(ValidateTarget.class).order()))
                .collect(Collectors.toList());
    }

    /**
     * Validates the AI model results for the specified function and identifier using the validators in the chain.
     * The validation is performed based on the target function's annotations defined in the validators.
     * If a target function is marked as "global" or is explicitly listed in the validator's target names,
     * the corresponding validator is used to validate the results.
     *
     * @param promptMessageContext Prompt Message context for calling openai chat completion api.
     * @return A {@code Mono<String>} representing the validated result as a string.
     */

    public Mono<String> validate(PromptMessageContext promptMessageContext) {
        return Flux.fromIterable(validators)
                .filter(validator -> {
                    ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
                    List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
                    return targetFunction.global() || targetFunctionList.contains(promptMessageContext.getFunctionName());
                })
                .concatMap(validator -> validator.validate(promptMessageContext)).last();
    }

    /**
     * Retrieves the list of ReactiveResultValidator instances that are applicable to the specified function name.
     * The applicable validators are filtered based on the target function's annotations defined in the validators.
     * If a target function is marked as "global" or is explicitly listed in the validator's target names,
     * the corresponding validator is included in the returned list.
     *
     * @param functionName The name of the function for which to retrieve applicable validators.
     * @return The list of applicable ReactiveResultValidator instances.
     */
    public List<ReactiveResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }
}
