package io.github.zezeg2.aisupport.ai.validator;

import lombok.Setter;

import java.util.List;


public class ExceptionValidatorChain extends ValidatorChain<ExceptionValidator> {

    @Setter
    protected Exception exception;
    public ExceptionValidatorChain(List<ExceptionValidator> validators) {
        super(validators);
    }

    @Override
    public String validate(String functionName, String target) {
        String result = target;
        for (ExceptionValidator validator : validators) {
            validator.setException(exception);
            if (validator.isRequired(functionName, result)) {
                result = validator.validate(functionName, result);
            }
        }
        return result;
    }
}
