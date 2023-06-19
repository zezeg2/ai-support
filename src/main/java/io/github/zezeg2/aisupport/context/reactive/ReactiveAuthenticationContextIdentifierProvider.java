package io.github.zezeg2.aisupport.context.reactive;

import jakarta.servlet.ServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ReactiveAuthenticationContextIdentifierProvider implements ReactiveContextIdentifierProvider {
    @Override
    public Mono<String> getId(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(Authentication::getName)
                .defaultIfEmpty("anonymous");
    }

    @Override
    public Mono<String> getId(ServerRequest request) {
        return request.principal()
                .cast(Authentication.class)
                .map(Authentication::getName)
                .defaultIfEmpty("anonymous");
    }
}
