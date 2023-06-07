package io.github.zezeg2.aisupport.ai.validator.chain;

import io.github.zezeg2.aisupport.ai.validator.ReactiveValidatable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.List;

public abstract class ReactiveValidatorChain<T extends ReactiveValidatable> {

    protected final List<T> validators;

    public ReactiveValidatorChain(List<T> validators) {
        this.validators = validators;
    }

    public abstract Flux<String> validate(ServerWebExchange exchange, String functionName, String target) throws Exception;
}
