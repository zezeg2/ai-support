package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;

public interface ReactiveValidatable<S> {
    Flux<String> validate(S idSource, String functionName) throws JsonProcessingException;
}
