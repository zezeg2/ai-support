package io.github.zezeg2.aisupport.core.validator;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Validatable<S> {
    S validate(String functionName) throws JsonProcessingException;
}
