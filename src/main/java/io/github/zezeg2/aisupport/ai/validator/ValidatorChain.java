package io.github.zezeg2.aisupport.ai.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidatorChain<T extends Validatable> {
    private final List<T> validators = new ArrayList<>();

    public void addValidator(T validator) {
        validators.add(validator);
    }

    public String validate(String target) {
        String result = target;
        for (T validator : validators) {
            if (validator.isRequired(result)) {
                result = validator.validate(result);
            }
        }
        return result;
    }
}
