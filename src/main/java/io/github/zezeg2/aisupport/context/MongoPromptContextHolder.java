package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;

public class MongoPromptContextHolder implements PromptContextHolder {

    private final MongoTemplate mongoTemplate;

    public MongoPromptContextHolder(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean contains(String namespace) {
        return mongoTemplate.collectionExists(namespace);
    }

    @Override
    public void savePrompt(String namespace, Prompt prompt) {
        mongoTemplate.save(prompt, namespace);
    }

    @Override
    public Prompt get(String namespace) {
        return mongoTemplate.findOne(Query.query(new Criteria()), Prompt.class, namespace);
    }

    @Override
    public PromptMessages getPromptChatMessages(String namespace, String identifier) {
        PromptMessages result = mongoTemplate.findOne(Query.query(Criteria.where("identifier").is(identifier)), PromptMessages.class, namespace);
        return result != null ? result : PromptMessages.builder()
                .identifier(identifier)
                .functionName(namespace)
                .content(new ArrayList<>()).build();
    }

    @Override
    public FeedbackMessages getFeedbackChatMessages(String namespace, String identifier) {
        FeedbackMessages result = mongoTemplate.findOne(Query.query(Criteria.where("identifier").is(identifier)), FeedbackMessages.class, namespace);
        String[] split = namespace.split(":");
        return result != null ? result : FeedbackMessages.builder()
                .identifier(identifier)
                .functionName(split[0])
                .validatorName(split[1])
                .content(new ArrayList<>()).build();
    }

    @Override
    public void savePromptMessages(String namespace, String identifier, ChatMessage message) {
        PromptMessages promptMessages = getPromptChatMessages(namespace, identifier);
        promptMessages.getContent().add(message);
        mongoTemplate.save(promptMessages, namespace);
    }

    @Override
    public void saveFeedbackMessages(String namespace, String identifier, ChatMessage message) {
        FeedbackMessages feedbackMessages = getFeedbackChatMessages(namespace, identifier);
        feedbackMessages.getContent().add(message);
        mongoTemplate.save(feedbackMessages, namespace);
    }
}
