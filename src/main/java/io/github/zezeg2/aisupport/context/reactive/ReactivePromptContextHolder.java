package io.github.zezeg2.aisupport.context.reactive;

import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import reactor.core.publisher.Mono;

public interface ReactivePromptContextHolder<S> {

    Mono<Boolean> containsPrompt(String functionName);

    Mono<Void> savePromptToContext(String functionName, ReactivePrompt<S> prompt);

    Mono<ReactivePrompt<S>> getPrompt(String functionName);
}
