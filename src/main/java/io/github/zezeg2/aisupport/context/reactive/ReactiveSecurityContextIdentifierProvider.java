package io.github.zezeg2.aisupport.context.reactive;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public class ReactiveSecurityContextIdentifierProvider {
    public Mono<String> getId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getName());
    }
}
