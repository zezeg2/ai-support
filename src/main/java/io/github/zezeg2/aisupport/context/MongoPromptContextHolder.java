package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MongoPromptContextHolder implements PromptContextHolder {

    private final MongoTemplate mongoTemplate;

    public MongoPromptContextHolder(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean contains(String namespace) {
        return mongoTemplate.exists(Query.query(Criteria.where("functionName").is(namespace)), Prompt.class);
    }

    @Override
    public void savePrompt(String namespace, Prompt prompt) {
        mongoTemplate.save(prompt);
    }

    @Override
    public Prompt get(String namespace) {
        return mongoTemplate.findOne(Query.query(Criteria.where("functionName").is(namespace)), Prompt.class);
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessagesContext(String namespace) {
        Prompt prompt = get(namespace);
        return prompt != null ? prompt.getPromptMessagesContext() : new HashMap<>();
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackMessagesContext(String namespace) {
        String[] split = namespace.split(":");
        Prompt prompt = get(split[0]);
        return prompt != null ? prompt.getFeedbackMessagesContext().get(split[1]) : new HashMap<>();
    }

    @Override
    public List<ChatMessage> getPromptChatMessages(String namespace, String identifier) {
        Map<String, List<ChatMessage>> context = getPromptMessagesContext(namespace);
        return context.getOrDefault(identifier, null);
    }

    @Override
    public List<ChatMessage> getFeedbackChatMessages(String namespace, String identifier) {
        Map<String, List<ChatMessage>> context = getFeedbackMessagesContext(namespace);
        return context.getOrDefault(identifier, null);
    }

    @Override
    public void savePromptMessagesContext(String namespace, String identifier, ChatMessage message) {
        Prompt prompt = get(namespace);
        if (prompt != null) {
            prompt.getPromptMessagesContext().get(identifier).add(message);
            savePrompt(namespace, prompt);
        }
    }

    @Override
    public void saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message) {
        String[] split = namespace.split(":");
        Prompt prompt = get(split[0]);
        if (prompt != null) {
            prompt.getFeedbackMessagesContext().get(split[1]).get(identifier).add(message);
            savePrompt(namespace, prompt);
        }
    }
}
