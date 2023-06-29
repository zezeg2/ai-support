package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class ReactiveMongoPromptContextHolder implements ReactivePromptContextHolder {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public ReactiveMongoPromptContextHolder(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Boolean> contains(String namespace) {
        return reactiveMongoTemplate.collectionExists(namespace);
    }

    @Override
    public Mono<Void> savePrompt(String namespace, Prompt prompt) {
        return reactiveMongoTemplate.save(prompt, namespace).then();
    }

    @Override
    public Mono<Prompt> get(String namespace) {
        return reactiveMongoTemplate.findOne(Query.query(new Criteria()), Prompt.class, namespace);
    }

    @Override
    public Mono<PromptMessages> getPromptChatMessages(String namespace, String identifier) {
        return reactiveMongoTemplate.findOne(Query.query(Criteria.where("identifier").is(identifier)), PromptMessages.class, namespace)
                .switchIfEmpty(Mono.defer(() -> {
                    PromptMessages promptMessages = PromptMessages.builder()
                            .identifier(identifier)
                            .functionName(namespace)
                            .content(new ArrayList<>()).build();
                    reactiveMongoTemplate.save(promptMessages, namespace);
                    return Mono.just(promptMessages);
                }));
    }

    @Override
    public Mono<FeedbackMessages> getFeedbackChatMessages(String namespace, String identifier) {
        return reactiveMongoTemplate.findOne(Query.query(Criteria.where("identifier").is(identifier)), FeedbackMessages.class, namespace)
                .switchIfEmpty(Mono.defer(() -> {
                    String[] split = namespace.split(":");
                    FeedbackMessages feedbackMessages = FeedbackMessages.builder()
                            .identifier(identifier)
                            .functionName(split[0])
                            .validatorName(split[1])
                            .content(new ArrayList<>()).build();
                    reactiveMongoTemplate.save(feedbackMessages, namespace);
                    return Mono.just(feedbackMessages);
                }));
    }

    @Override
    public Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message) {
        return getPromptChatMessages(namespace, identifier)
                .doOnNext(promptMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && promptMessages.getContent().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))){
                        promptMessages.getContent().get(0).setContent(message.getContent());
                    } else {
                        promptMessages.getContent().add(message);
                    }
                })
                .flatMap(promptMessages -> reactiveMongoTemplate.save(promptMessages, namespace))
                .then();
    }

    @Override
    public Mono<Void> saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        return getFeedbackChatMessages(namespace, identifier)
                .doOnNext(feedbackMessages -> {
                    if (message.getRole().equals(ROLE.SYSTEM.getValue()) && feedbackMessages.getContent().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))){
                        feedbackMessages.getContent().get(0).setContent(message.getContent());
                    } else {
                        feedbackMessages.getContent().add(message);
                    }
                })
                .flatMap(feedbackMessages -> reactiveMongoTemplate.save(feedbackMessages, namespace))
                .then();
    }

    @Override
    public Mono<Void> deleteLastPromptMessage(String namespace, String identifier, Integer n) {
        return getPromptChatMessages(namespace, identifier)
                .filter(promptMessages -> !promptMessages.getContent().isEmpty())
                .doOnNext(promptMessages -> {
                    List<ChatMessage> content = promptMessages.getContent();
                    if (!content.isEmpty()) {
                        int removeIndex = Math.max(0, content.size() - n);
                        content.subList(removeIndex, content.size()).clear();
                    }
                })
                .flatMap(promptMessages -> reactiveMongoTemplate.save(promptMessages, namespace).then());
    }

    @Override
    public Mono<Void> deleteLastFeedbackMessage(String namespace, String identifier, Integer n) {
        return getFeedbackChatMessages(namespace, identifier)
                .filter(feedbackMessages -> !feedbackMessages.getContent().isEmpty())
                .doOnNext(feedbackMessages -> {
                    List<ChatMessage> content = feedbackMessages.getContent();
                    int removeIndex = Math.max(0, content.size() - n);
                    content.subList(removeIndex, content.size()).clear();
                })
                .flatMap(feedbackMessages -> reactiveMongoTemplate.save(feedbackMessages, namespace).then());
    }

}
