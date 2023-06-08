package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ReactiveSessionContextIdentifierProvider implements ReactiveContextIdentifierProvider<ServerWebExchange> {
    public Mono<String> getId(ServerWebExchange exchange) {
        return exchange.getSession().flatMap(webSession -> Mono.just(webSession.getId()));
    }
}
