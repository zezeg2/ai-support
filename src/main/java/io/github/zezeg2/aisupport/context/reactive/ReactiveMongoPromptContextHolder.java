package io.github.zezeg2.aisupport.context.reactive;

import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePrompt;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

public class ReactiveMongoPromptContextHolder implements ReactivePromptContextHolder {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public ReactiveMongoPromptContextHolder(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Boolean> containsPrompt(String functionName) {
        return reactiveMongoTemplate.exists(Query.query(Criteria.where("functionName").is(functionName)), ReactivePrompt.class);
    }

    @Override
    public Mono<Void> savePromptToContext(String functionName, ReactivePrompt prompt) {
        return reactiveMongoTemplate.save(prompt, functionName)
                .then();
    }

    @Override
    public Mono<ReactivePrompt> getPrompt(String functionName) {
        return reactiveMongoTemplate.findOne(Query.query(Criteria.where("functionName").is(functionName)), ReactivePrompt.class);
    }
}
