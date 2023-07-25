package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

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
    public <T extends MessageContext> T getContext(ContextType contextType, String namespace, String identifier) {
        T result = (T) mongoTemplate.findOne(
                Query.query(Criteria.where("identifier").is(identifier)),
                contextType.getContextClass(),
                namespace
        );

        if (result == null) {
            String[] split = namespace.split(":");
            result = contextType == ContextType.PROMPT
                    ? (T) PromptMessageContext.builder().identifier(identifier).functionName(namespace).messages(new ArrayList<>()).build()
                    : (T) FeedbackMessageContext.builder().identifier(identifier).functionName(split[0]).validatorName(split[1]).messages(new ArrayList<>()).build();
            saveContext(contextType, result);
        }
        return result;
    }

    @Override
    public void saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message) {
        MessageContext messageContext = getContext(contextType, namespace, identifier);
        if (message.getRole().equals(ROLE.SYSTEM.getValue()) && messageContext.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
            messageContext.getMessages().get(0).setContent(message.getContent());
        } else {
            messageContext.getMessages().add(message);
        }
        mongoTemplate.save(messageContext, namespace);
    }

    @Override
    public void saveContext(ContextType contextType, MessageContext messageContext) {
        if (contextType == ContextType.PROMPT) {
            mongoTemplate.save(messageContext, messageContext.getFunctionName());
        } else {
            mongoTemplate.save(messageContext, messageContext.getFunctionName() + ":" + ((FeedbackMessageContext) messageContext).getValidatorName());
        }
    }

    @Override
    public void deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n) {
        MessageContext messageContext = getContext(contextType, namespace, identifier);

        List<ChatMessage> content = messageContext.getMessages();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
        mongoTemplate.save(messageContext, namespace);
    }
}
