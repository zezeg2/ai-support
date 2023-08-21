package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public class MongoPromptContextHolder implements PromptContextHolder {

    private final MongoTemplate mongoTemplate;

    private final SequenceGenerator sequenceGenerator;

    public MongoPromptContextHolder(MongoTemplate mongoTemplate, SequenceGenerator sequenceGenerator) {
        this.mongoTemplate = mongoTemplate;
        this.sequenceGenerator = sequenceGenerator;
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
    public <T extends MessageContext> T createMessageContext(ContextType contextType, String namespace, String identifier) {
        String[] split = namespace.split(":");
        long seq = sequenceGenerator.generateSequence(MessageContext.getSequenceName(contextType == ContextType.PROMPT ? namespace : namespace.replace(":", "_"), identifier), identifier);
        T messageContext = (T) (contextType == ContextType.PROMPT
                ? PromptMessageContext.builder().seq(seq).functionName(namespace).identifier(identifier).messages(new ArrayList<>()).build()
                : FeedbackMessageContext.builder().seq(seq).functionName(split[0]).validatorName(split[1]).identifier(identifier).messages(new ArrayList<>()).build());

        mongoTemplate.save(messageContext, namespace);
        return messageContext;
    }

    @Override
    public void saveMessageContext(ContextType contextType, MessageContext messageContext) {
        mongoTemplate.save(messageContext, messageContext.getNamespace());
    }

    @Override
    public void deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n) {
        List<ChatMessage> content = messageContext.getMessages();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
        mongoTemplate.save(messageContext, messageContext.getNamespace());
    }
}
