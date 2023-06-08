package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import io.github.zezeg2.aisupport.ai.validator.ValidateTarget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class ReactiveResultValidatorChain extends ResultValidatorChain<Mono<String>, ReactiveResultValidator> {
    public ReactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        super(validators);
    }

    @Override
    public Mono<String> validate(String functionName, String target) {
        if (validators.isEmpty()) {
            return Mono.just(target);
        }

        return Flux.fromIterable(validators)
                .filter(validator -> {
                    ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
                    List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
                    return targetFunction.global() || targetFunctionList.contains(functionName);
                })
                .concatMap(validator -> validator.validate(functionName)).last();
    }
}
