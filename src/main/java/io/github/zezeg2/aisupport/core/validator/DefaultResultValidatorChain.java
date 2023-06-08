package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;
import java.util.List;

public class DefaultResultValidatorChain extends ResultValidatorChain<String, DefaultResultValidator> {
    public DefaultResultValidatorChain(List<DefaultResultValidator> validators) {
        super(validators);
    }

    @Override
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

}
