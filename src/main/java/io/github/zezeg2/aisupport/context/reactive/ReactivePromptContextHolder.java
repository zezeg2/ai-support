package io.github.zezeg2.aisupport.context.reactive;

import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import reactor.core.publisher.Mono;

public interface ReactivePromptContextHolder {

    Mono<Boolean> containsPrompt(String functionName);

    Mono<Void> savePromptToContext(String functionName, Prompt prompt);

    Mono<Prompt> getPrompt(String functionName);
}
