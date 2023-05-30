package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.function.prompt.Prompt;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

public class MongoPromptContextHolder implements PromptContextHolder {
    private final MongoTemplate mongoTemplate;
    private final ContextIdentifierProvider identifierProvider;

    public MongoPromptContextHolder(MongoTemplate mongoTemplate, ContextIdentifierProvider identifierProvider) {
        this.mongoTemplate = mongoTemplate;
        this.identifierProvider = identifierProvider;
    }

    @Override
    public boolean containsPrompt(String functionName) {
        return mongoTemplate.exists(Query.query(Criteria.where("functionName").is(functionName)), Prompt.class);
    }

    @Override
    public void addPromptToContext(String functionName, Prompt prompt) {
        mongoTemplate.save(prompt, functionName);
    }

    @Override
    public Prompt getPrompt(String functionName) {
        return mongoTemplate.findOne(Query.query(Criteria.where("functionName").is(functionName)), Prompt.class);
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessageContext(String functionName) {
        Prompt prompt = getPrompt(functionName);
        return prompt != null ? prompt.getPromptMessageContext() : null;
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackAssistantMessageContext(String functionName) {
        Prompt prompt = getPrompt(functionName);
        return prompt != null ? prompt.getFeedbackAssistantContext() : null;
    }

}
