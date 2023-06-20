package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface ReactiveContextIdentifierProvider {
    Mono<String> getId(ServerWebExchange exchange);

    Mono<String> getId(ServerRequest request);
}
