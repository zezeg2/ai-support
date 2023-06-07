package io.github.zezeg2.aisupport.ai.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

public interface ReactiveValidatable {
    Flux<String> validate(ServerWebExchange exchange, String functionName) throws JsonProcessingException;
}
