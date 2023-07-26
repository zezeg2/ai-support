package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalMemoryPromptContextHolder implements PromptContextHolder {
    private static final Map<String, Prompt> promptRegistry = new ConcurrentHashMap<>();
    private static final Map<ContextType, Map<String, CopyOnWriteArrayList<MessageContext>>> contextRegistry =
            new ConcurrentHashMap<>(Map.of(ContextType.PROMPT, new ConcurrentHashMap<>(), ContextType.FEEDBACK, new ConcurrentHashMap<>()));


    @Override
    public boolean contains(String namespace) {
        return promptRegistry.containsKey(namespace);
    }

    @Override
    public void savePrompt(String namespace, Prompt prompt) {
        promptRegistry.put(namespace, prompt);
    }

    @Override
    public Prompt get(String namespace) {
        return promptRegistry.get(namespace);
    }


    @Override
    public <T extends MessageContext> T createMessageContext(ContextType contextType, String namespace, String identifier) {
        Map<String, CopyOnWriteArrayList<MessageContext>> selectedRegistry = contextRegistry.get(contextType);
        if (!selectedRegistry.containsKey(namespace)) selectedRegistry.put(namespace, new CopyOnWriteArrayList<>());
        String[] split = namespace.split(":");
        CopyOnWriteArrayList<MessageContext> messageContextList = selectedRegistry.get(identifier);
        Long seq = (long) messageContextList.size();
        T messageContext = (T) (contextType == ContextType.PROMPT
                ? PromptMessageContext.builder().seq(seq).functionName(namespace).identifier(identifier).messages(new ArrayList<>()).build()
                : FeedbackMessageContext.builder().seq(seq).functionName(split[0]).validatorName(split[1]).identifier(identifier).messages(new ArrayList<>()).build());
        messageContextList.add(messageContext);
        return messageContext;
    }

    @Override
    public void saveMessageContext(ContextType contextType, MessageContext messageContext) {
        String namcespace = contextType == ContextType.PROMPT ? messageContext.getFunctionName() : messageContext.getFunctionName() + ":" + ((FeedbackMessageContext) messageContext).getValidatorName();
        MessageContext origin = contextRegistry.get(contextType).get(namcespace).stream()
                .filter(ctx -> ctx.getIdentifier().equals(messageContext.getIdentifier()))
                .findFirst().orElseThrow();
        origin.setMessages(messageContext.getMessages());
    }


    @Override
    public void deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n) {
        List<ChatMessage> content = messageContext.getMessages();
        if (!content.isEmpty()) {
            int removeIndex = Math.max(0, content.size() - n);
            content.subList(removeIndex, content.size()).clear();
        }
    }
}
