package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Validatable {
    String validate(String functionName) throws JsonProcessingException;
}
