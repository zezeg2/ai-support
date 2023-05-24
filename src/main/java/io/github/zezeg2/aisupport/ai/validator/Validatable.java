package io.github.zezeg2.aisupport.ai.validator;

public interface Validatable {
    boolean isRequired(String target);

    String validate(String target);
}
