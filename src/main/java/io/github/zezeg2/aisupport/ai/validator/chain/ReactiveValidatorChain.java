package io.github.zezeg2.aisupport.ai.validator.chain;

import io.github.zezeg2.aisupport.ai.validator.ReactiveValidatable;
import reactor.core.publisher.Flux;

import java.util.List;

public abstract class ReactiveValidatorChain<S, T extends ReactiveValidatable<S>> {

    protected final List<T> validators;

    public ReactiveValidatorChain(List<T> validators) {
        this.validators = validators;
    }

    public abstract Flux<String> validate(S idSource, String functionName, String target) throws Exception;
}
