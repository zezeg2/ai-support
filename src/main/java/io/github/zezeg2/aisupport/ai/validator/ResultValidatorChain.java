package io.github.zezeg2.aisupport.ai.validator;

import java.util.Arrays;
import java.util.List;

public class ResultValidatorChain extends ValidatorChain<ResultValidator> {
    private final Class<?> clazz;

    public ResultValidatorChain(List<ResultValidator> validators, Class<?> clazz) {
        super(validators);
        this.clazz = clazz;
    }

    @Override
    public String validate(String functionName, String target) {
        List<ResultValidator> validators = selectValidators(clazz);
        String result = target;
        for (ResultValidator validator : validators) {
            if (validator.isRequired(functionName, result)) {
                result = validator.validate(functionName, result);
            }
        }
        return result;
    }

    public List<ResultValidator> selectValidators(Class<?> clazz){
        return validators.stream().filter(validator -> Arrays.stream(validator.getClass().getInterfaces()).toList().contains(clazz) || clazz.equals(Object.class)).toList();
    }
}
