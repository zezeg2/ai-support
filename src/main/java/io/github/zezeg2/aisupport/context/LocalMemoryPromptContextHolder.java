package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.ROLE;
import io.github.zezeg2.aisupport.core.function.prompt.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public <T extends MessageContext> T getContext(ContextType contextType, String namespace, String identifier) {
        Map<String, CopyOnWriteArrayList<MessageContext>> selectedRegistry = contextRegistry.get(contextType);
        if (!selectedRegistry.containsKey(namespace)) selectedRegistry.put(namespace, new CopyOnWriteArrayList<>());
        return (T) selectedRegistry.get(namespace).stream()
                .filter(context -> context.getIdentifier().equals(identifier)).findFirst()
                .orElse(contextType == ContextType.PROMPT ? PromptMessageContext.builder().identifier(identifier).messages(new CopyOnWriteArrayList<>()).build()
                        : FeedbackMessageContext.builder().identifier(identifier).messages(new CopyOnWriteArrayList<>()).build());
    }

    @Override
    public void saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message) {
        if (!contextRegistry.get(contextType).containsKey(namespace)) {
            contextRegistry.get(contextType).put(namespace, new CopyOnWriteArrayList<>());
        }
        MessageContext messageContext = getContext(contextType, namespace, identifier);
        if (message.getRole().equals(ROLE.SYSTEM.getValue()) && messageContext.getMessages().stream().anyMatch(chatMessage -> chatMessage.getRole().equals(ROLE.SYSTEM.getValue()))) {
            messageContext.getMessages().get(0).setContent(message.getContent());
        } else {
            messageContext.getMessages().add(message);
        }

        if (contextRegistry.get(contextType).get(namespace).stream()
                .filter(ctx -> ctx.getIdentifier().equals(identifier)).findFirst().isEmpty()) {
            contextRegistry.get(contextType).get(namespace).add(messageContext);
        }
    }


    @Override
    public void saveContext(ContextType contextType, MessageContext messageContext) {
        String key = contextType == ContextType.PROMPT ? messageContext.getFunctionName() : messageContext.getFunctionName() + ":" + ((FeedbackMessageContext) messageContext).getValidatorName();
        MessageContext origin = contextRegistry.get(contextType).get(key).stream()
                .filter(ctx -> ctx.getIdentifier().equals(messageContext.getIdentifier()))
                .findFirst().orElseThrow();
        origin.setMessages(messageContext.getMessages());
    }

    @Override
    public void deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n) {
        List<MessageContext> baseMessageContextList = contextRegistry.get(contextType).get(namespace);
        if (baseMessageContextList != null) {
            Optional<MessageContext> baseContextOptional = baseMessageContextList.stream()
                    .filter(baseContext -> baseContext.getIdentifier().equals(identifier)).findFirst();
            baseContextOptional.ifPresent(baseContext -> {
                List<ChatMessage> content = baseContext.getMessages();
                if (!content.isEmpty()) {
                    int removeIndex = Math.max(0, content.size() - n);
                    content.subList(removeIndex, content.size()).clear();
                }
            });
        }
    }
}
