package io.github.zezeg2.aisupport.core.validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResultValidatorChain {

    protected final List<ResultValidator> validators;

    public ResultValidatorChain(List<ResultValidator> validators) {
        this.validators = validators;
    }

    public String validate(String functionName, String identifier, Map<String, Object> lastUserInput, String target) {
        String result = target;
        for (ResultValidator validator : validators) {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            if (targetFunction.global() || targetFunctionList.contains(functionName)) {
                result = validator.validate(functionName, identifier, lastUserInput);
            }
        }
        return result;
    }

    public List<ResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
