package io.github.zezeg2.aisupport.context.reactive;

import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import reactor.core.publisher.Mono;

public interface ReactivePromptContextHolder {

    Mono<Boolean> containsPrompt(String functionName);

    Mono<Void> savePromptToContext(String functionName, ReactivePrompt prompt);

    Mono<ReactivePrompt> getPrompt(String functionName);
}
