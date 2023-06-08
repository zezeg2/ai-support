package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

public class ReactiveSessionContextIdentifierProvider implements ReactiveContextIdentifierProvider {
    @Override
    public Mono<String> getId() {
        return null;
    }

    public Mono<String> getId(ServerWebExchange exchange, String s) {
        return exchange.getSession().map(WebSession::getId);
    }
}
