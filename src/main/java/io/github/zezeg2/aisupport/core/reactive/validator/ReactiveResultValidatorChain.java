package io.github.zezeg2.aisupport.core.reactive.validator;

import io.github.zezeg2.aisupport.core.validator.ValidateTarget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;


public class ReactiveResultValidatorChain {

    protected final List<ReactiveResultValidator> validators;

    public ReactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        this.validators = validators;
    }

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

    public List<ReactiveResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }
}
