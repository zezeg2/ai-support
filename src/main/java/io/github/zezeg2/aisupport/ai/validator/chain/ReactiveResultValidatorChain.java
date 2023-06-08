package io.github.zezeg2.aisupport.ai.validator.chain;

import io.github.zezeg2.aisupport.ai.validator.ReactiveResultValidator;
import io.github.zezeg2.aisupport.ai.validator.ValidateTarget;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
public class ReactiveResultValidatorChain<S> extends ReactiveValidatorChain<S, ReactiveResultValidator<S>> {
    public ReactiveResultValidatorChain(List<ReactiveResultValidator<S>> validators) {
        super(validators);
    }

    @Override
    public Flux<String> validate(S idSource, String functionName, String target) {
        return Flux.fromIterable(validators)
                .filter(validator -> {
                    ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
                    List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
                    return targetFunction.global() || targetFunctionList.contains(functionName);
                })
                .concatMap(validator -> validator.validate(idSource, functionName).last(target));
    }


    public List<ReactiveResultValidator<S>> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
