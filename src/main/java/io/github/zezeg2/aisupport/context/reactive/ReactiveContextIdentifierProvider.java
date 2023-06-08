package io.github.zezeg2.aisupport.context.reactive;

import reactor.core.publisher.Mono;

public interface ReactiveContextIdentifierProvider<S> {
    Mono<String> getId(S idSource);
}
