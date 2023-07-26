package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class ReactiveMongoPromptContextHolder implements ReactivePromptContextHolder {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private final ReactiveSequenceGenerator sequenceGenerator;

    public ReactiveMongoPromptContextHolder(ReactiveMongoTemplate reactiveMongoTemplate, ReactiveSequenceGenerator sequenceGenerator) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.sequenceGenerator = sequenceGenerator;
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
    public <T extends MessageContext> Mono<T> createMessageContext(ContextType contextType, String namespace, String identifier) {
        return Mono.just(namespace)
                .map(n -> n.split(":"))
                .zipWith(sequenceGenerator.generateSequence(MessageContext.getSequenceName(
                                        contextType == ContextType.PROMPT ? namespace : namespace.replace(":", "_"), identifier), identifier)
                                .map(Long::valueOf),
                        (split, seq) -> (T) (contextType == ContextType.PROMPT
                                ? PromptMessageContext.builder().seq(seq).functionName(namespace).identifier(identifier).messages(new ArrayList<>()).build()
                                : FeedbackMessageContext.builder().seq(seq).functionName(split[0]).validatorName(split[1]).identifier(identifier).messages(new ArrayList<>()).build()))
                .flatMap(messageContext -> reactiveMongoTemplate.save(messageContext, namespace));
    }

    @Override
    public Mono<Void> saveMessageContext(ContextType contextType, MessageContext messageContext) {
        return reactiveMongoTemplate.save(messageContext, messageContext.getNamespace()).then();
    }

    @Override
    public Mono<Void> deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n) {
        return Mono.defer(() -> {
            List<ChatMessage> content = messageContext.getMessages();
            if (!content.isEmpty()) {
                int removeIndex = Math.max(0, content.size() - n);
                content.subList(removeIndex, content.size()).clear();
            }
            return reactiveMongoTemplate.save(messageContext, messageContext.getNamespace()).then();
        });
    }
}
