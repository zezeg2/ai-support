package io.github.zezeg2.aisupport.ai.function;

import io.github.zezeg2.aisupport.ai.function.argument.Argument;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveAIFunction<T> {

    Mono<T> execute(ServerWebExchange exchange, List<Argument<?>> args) throws Exception;

    Mono<T> execute(ServerWebExchange exchange, List<Argument<?>> args, AIModel model) throws Exception;

    String buildResultFormat();

    String createFunction(List<Argument<?>> args);
}
