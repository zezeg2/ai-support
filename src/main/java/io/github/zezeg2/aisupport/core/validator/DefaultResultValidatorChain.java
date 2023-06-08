package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidator;

import java.util.Arrays;
import java.util.List;

public class DefaultResultValidatorChain {

    protected final List<DefaultResultValidator> validators;

    public DefaultResultValidatorChain(List<DefaultResultValidator> validators) {
        this.validators = validators;
    }

    public String validate(String functionName, String target) {
        String result = target;
        for (DefaultResultValidator validator : validators) {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            if (targetFunction.global() || targetFunctionList.contains(functionName)) {
                try {
                    result = validator.validate(functionName);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    public List<DefaultResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
