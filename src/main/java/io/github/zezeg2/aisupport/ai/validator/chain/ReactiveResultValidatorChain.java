package io.github.zezeg2.aisupport.ai.validator.chain;

import io.github.zezeg2.aisupport.ai.validator.ReactiveResultValidator;
import io.github.zezeg2.aisupport.ai.validator.ValidateTarget;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
public class ReactiveResultValidatorChain extends ReactiveValidatorChain<ReactiveResultValidator> {
    public ReactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        super(validators);
    }

    @Override
    public Flux<String> validate(ServerWebExchange exchange, String functionName, String target) {
        return Flux.fromIterable(validators)
                .filter(validator -> {
                    ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
                    List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
                    return targetFunction.global() || targetFunctionList.contains(functionName);
                })
                .concatMap(validator -> validator.validate(exchange, functionName).last(target));
    }


    public List<ReactiveResultValidator> peekValidators(String functionName) {
        return validators.stream().filter(validator -> {
            ValidateTarget targetFunction = validator.getClass().getAnnotation(ValidateTarget.class);
            List<String> targetFunctionList = Arrays.stream(targetFunction.names()).toList();
            return targetFunction.global() || targetFunctionList.contains(functionName);
        }).toList();
    }

}
