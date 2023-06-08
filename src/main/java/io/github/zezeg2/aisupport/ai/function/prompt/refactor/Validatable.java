package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Validatable<S> {
    S validate(String functionName) throws JsonProcessingException;
}
