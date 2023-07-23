package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.*;

/**
 * The PromptContextHolder interface defines the contract for managing prompt-related information in the context.
 *
 * @since 1.0
 */
public interface PromptContextHolder {

    /**
     * Checks if the context contains the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @return true if the prompt information exists in the context, false otherwise.
     */
    boolean contains(String namespace);

    /**
     * Saves the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @param prompt    The prompt to save.
     */
    void savePrompt(String namespace, Prompt prompt);

    /**
     * Retrieves the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @return The prompt associated with the namespace, or null if not found.
     */
    Prompt get(String namespace);

    <T extends MessageContext> T getContext(ContextType contextType, String namespace, String identifier);

    void saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message);

    void saveContext(ContextType contextType, MessageContext messageContext);

    void deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n);

}
