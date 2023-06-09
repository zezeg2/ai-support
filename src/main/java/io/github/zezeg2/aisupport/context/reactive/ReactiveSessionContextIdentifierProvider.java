package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

public class ReactiveSessionContextIdentifierProvider implements ReactiveContextIdentifierProvider {
    @Override
    public Mono<String> getId(ServerWebExchange exchange) {
        return exchange.getSession().map(WebSession::getId).log();
    }
}
