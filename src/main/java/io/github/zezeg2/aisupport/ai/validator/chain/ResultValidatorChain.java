package io.github.zezeg2.aisupport.ai.validator.chain;

import io.github.zezeg2.aisupport.ai.validator.ResultValidator;
import io.github.zezeg2.aisupport.ai.validator.ValidateTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ResultValidatorChain extends ValidatorChain<ResultValidator> {
    public ResultValidatorChain(List<ResultValidator> validators) {
        super(validators);
    }

    @Override
    public String validate(String functionName, String target) {
        String result = target;
        for (ResultValidator validator : validators) {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            if (targetFunction.global() || targetFunctionList.contains(functionName)) {
                result = validator.validate(functionName);
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
