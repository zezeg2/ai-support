package io.github.zezeg2.aisupport.ai.function;

import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveAIFunction<T, S> {

    Mono<T> execute(S idSource, List<Argument<?>> args);

    Mono<T> execute(S idSource, List<Argument<?>> args, AIModel model);

    String buildResultFormat();

    String createFunction(List<Argument<?>> args);
}
