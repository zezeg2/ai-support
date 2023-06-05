package io.github.zezeg2.aisupport.context.servlet;

import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class MongoPromptContextHolder implements PromptContextHolder {
    private final MongoTemplate mongoTemplate;

    public MongoPromptContextHolder(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean containsPrompt(String functionName) {
        return mongoTemplate.exists(Query.query(Criteria.where("functionName").is(functionName)), Prompt.class);
    }

    @Override
    public void savePromptToContext(String functionName, Prompt prompt) {
        mongoTemplate.save(prompt, functionName);
    }

    @Override
    public Prompt getPrompt(String functionName) {
        return mongoTemplate.findOne(Query.query(Criteria.where("functionName").is(functionName)), Prompt.class);
    }
}
