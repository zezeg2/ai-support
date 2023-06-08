package io.github.zezeg2.aisupport.core.validator;

import java.util.List;

public abstract class ValidatorChain<T extends Validatable<?>> {

    protected final List<T> validators;

    public ValidatorChain(List<T> validators) {
        this.validators = validators;
    }

    public abstract String validate(String functionName, String target) throws Exception;
}
