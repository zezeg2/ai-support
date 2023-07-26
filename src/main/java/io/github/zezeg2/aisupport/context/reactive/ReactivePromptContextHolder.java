package io.github.zezeg2.aisupport.context.reactive;

import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.MessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import reactor.core.publisher.Mono;

/**
 * The ReactivePromptContextHolder interface defines the contract for managing prompt-related information in a reactive context.
 */
public interface ReactivePromptContextHolder {

    /**
     * Checks if the context contains the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @return A Mono emitting {@code true} if the prompt information exists in the context, {@code false} otherwise.
     */
    Mono<Boolean> contains(String namespace);

    /**
     * Saves the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @param prompt    The prompt to save.
     * @return A Mono representing the completion of the save operation.
     */
    Mono<Void> savePrompt(String namespace, Prompt prompt);

    /**
     * Retrieves the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @return A Mono emitting the prompt associated with the namespace, or {@code null} if not found.
     */
    Mono<Prompt> get(String namespace);

    <T extends MessageContext> Mono<T> createMessageContext(ContextType contextType, String namespace, String identifier);

    /**
     * Saves a message context of the specified type in a reactive manner.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext The message context to save.
     * @return A Mono representing the completion of the save operation.
     */
    Mono<Void> saveMessageContext(ContextType contextType, MessageContext messageContext);

    /**
     * Deletes a specified number of messages from the end of the message context of the specified type, namespace,
     * and identifier in a reactive manner.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext Message context for calling openai chat completion api.
     * @param n              The number of messages to delete.
     * @return A Mono representing the completion of the delete operation.
     */
    Mono<Void> deleteMessagesFromLast(ContextType contextType, MessageContext messageContext, Integer n);
}
