package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public class ReactiveSecurityContextIdentifierProvider implements ReactiveContextIdentifierProvider<Authentication> {
    public Mono<String> getId(Authentication authentication) {
        return Mono.just(authentication.getName());
    }
}
