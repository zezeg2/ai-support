package io.github.zezeg2.aisupport.context;

import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.MessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;

/**
 * The PromptContextHolder interface defines the contract for managing prompt-related information in the context.
 */
public interface PromptContextHolder {
    /**
     * Checks if the context contains the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @return {@code true} if the prompt information exists in the context, {@code false} otherwise.
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
     * @return The prompt associated with the namespace, or {@code null} if not found.
     */
    Prompt get(String namespace);

    /**
     * Creates a new message context of the specified type for the given namespace and identifier.
     *
     * @param contextType The type of context (prompt or feedback).
     * @param namespace   The namespace of the message context.
     * @param identifier  The identifier for the message context.
     * @param <T>         A generic type parameter that extends MessageContext.
     * @return A message context of the specified type.
     */
    <T extends MessageContext> T createMessageContext(ContextType contextType, String namespace, String identifier);

    /**
     * Saves a message context of the specified type.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext The message context to save.
     */
    void saveMessageContext(ContextType contextType, MessageContext messageContext);

    /**
     * Deletes a specified number of messages from the end of the message context of the specified type, namespace, and identifier.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext Message context for calling openai chat completion api
     * @param n              The number of messages to delete.
     */
    void deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n);
}
