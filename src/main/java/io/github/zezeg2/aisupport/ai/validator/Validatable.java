package io.github.zezeg2.aisupport.ai.validator;

public interface Validatable {
    boolean isRequired(String functionName, String target);

    String validate(String functionName, String target);
}
