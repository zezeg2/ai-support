package io.github.zezeg2.aisupport.context.reactive;

import reactor.core.publisher.Mono;

public interface ReactiveIdentifierProvider {
    Mono<String> get();
}
