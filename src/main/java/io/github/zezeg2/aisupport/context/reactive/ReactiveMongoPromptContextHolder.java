package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ReactiveMongoPromptContextHolder implements ReactivePromptContextHolder {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public ReactiveMongoPromptContextHolder(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Boolean> contains(String namespace) {
        return reactiveMongoTemplate.exists(Query.query(Criteria.where("functionName").is(namespace)), Prompt.class);
    }

    @Override
    public Mono<Void> savePrompt(String namespace, Prompt prompt) {
        return reactiveMongoTemplate.save(prompt).then();
    }

    @Override
    public Mono<Prompt> get(String namespace) {
        return reactiveMongoTemplate.findOne(Query.query(Criteria.where("functionName").is(namespace)), Prompt.class);
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getPromptMessagesContext(String namespace) {
        return get(namespace)
                .map(Prompt::getPromptMessagesContext);
    }

    @Override
    public Mono<Map<String, List<ChatMessage>>> getFeedbackMessagesContext(String namespace) {
        String[] split = namespace.split(":");
        return get(split[0])
                .map(prompt -> prompt.getFeedbackMessagesContext().get(split[1]));
    }

    @Override
    public Mono<List<ChatMessage>> getPromptChatMessages(String namespace, String identifier) {
        return getPromptMessagesContext(namespace)
                .map(context -> context.getOrDefault(identifier, null));
    }

    @Override
    public Mono<List<ChatMessage>> getFeedbackChatMessages(String namespace, String identifier) {
        return getFeedbackMessagesContext(namespace)
                .map(context -> context.getOrDefault(identifier, null));
    }

    @Override
    public Mono<Void> savePromptMessagesContext(String namespace, String identifier, ChatMessage message) {
        return get(namespace)
                .doOnSuccess(prompt -> {
                    if (prompt != null) {
                        prompt.getPromptMessagesContext().get(identifier).add(message);
                    }
                })
                .flatMap(prompt -> reactiveMongoTemplate.save(prompt).then());
    }

    @Override
    public Mono<Void> saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message) {
        String[] split = namespace.split(":");
        return get(split[0])
                .doOnSuccess(prompt -> {
                    if (prompt != null) {
                        prompt.getFeedbackMessagesContext().get(split[1]).get(identifier).add(message);
                    }
                })
                .flatMap(prompt -> reactiveMongoTemplate.save(prompt).then());
    }
}
