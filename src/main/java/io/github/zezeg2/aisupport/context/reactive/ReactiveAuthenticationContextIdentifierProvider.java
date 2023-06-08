package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ReactiveAuthenticationContextIdentifierProvider implements ReactiveContextIdentifierProvider {
    @Override
    public Mono<String> getId() {
        return Mono.empty();
    }

    public Mono<String> getId(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(Authentication::getName)
                .defaultIfEmpty("anonymous");
    }
}
