package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
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

    /**
     * Retrieves the message context of the specified type for the given namespace and identifier in a reactive manner.
     *
     * @param contextType The type of context (prompt or feedback).
     * @param namespace   The namespace of the message context.
     * @param identifier  The identifier of the chat context.
     * @return A Mono emitting the message context of the specified type, or {@code null} if not found.
     */
    <T extends MessageContext> Mono<T> getContext(ContextType contextType, String namespace, String identifier);

    /**
     * Saves a chat message in the context of the specified type, namespace, and identifier in a reactive manner.
     *
     * @param contextType The type of context (prompt or feedback).
     * @param namespace   The namespace of the message context.
     * @param identifier  The identifier of the chat context.
     * @param message     The chat message to save.
     * @return A Mono representing the completion of the save operation.
     */
    Mono<Void> saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message);

    /**
     * Saves a message context of the specified type in a reactive manner.
     *
     * @param contextType    The type of context (prompt or feedback).
     * @param messageContext The message context to save.
     * @return A Mono representing the completion of the save operation.
     */
    Mono<Void> saveContext(ContextType contextType, MessageContext messageContext);

    /**
     * Deletes a specified number of messages from the end of the message context of the specified type, namespace,
     * and identifier in a reactive manner.
     *
     * @param contextType The type of context (prompt or feedback).
     * @param namespace   The namespace of the message context.
     * @param identifier  The identifier of the chat context.
     * @param n           The number of messages to delete.
     * @return A Mono representing the completion of the delete operation.
     */
    Mono<Void> deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n);
}
