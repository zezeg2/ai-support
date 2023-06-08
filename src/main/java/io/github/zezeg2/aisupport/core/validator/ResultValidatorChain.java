package io.github.zezeg2.aisupport.core.validator;


import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;

import java.util.Arrays;
import java.util.List;

public abstract class ResultValidatorChain<S, V extends ResultValidator<S, ? extends PromptManager<?>>> {
    protected final List<V> validators;

    public ResultValidatorChain(List<V> validators) {
        this.validators = validators;
    }

    public abstract S validate(String functionName, String target);

    public List<V> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
