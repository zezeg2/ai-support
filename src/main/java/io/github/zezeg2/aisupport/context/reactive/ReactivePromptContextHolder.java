package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.ContextType;
import io.github.zezeg2.aisupport.core.function.prompt.MessageContext;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import reactor.core.publisher.Mono;

/**
 * The ReactivePromptContextHolder interface defines the contract for managing prompt-related information in a reactive context.
 *
 * @since 1.0
 */
public interface ReactivePromptContextHolder {

    /**
     * Checks if the context contains the prompt information for the given namespace.
     *
     * @param namespace The namespace of the prompt.
     * @return A Mono emitting true if the prompt information exists in the context, false otherwise.
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
     * @return A Mono emitting the prompt associated with the namespace, or null if not found.
     */
    Mono<Prompt> get(String namespace);


    <T extends MessageContext> Mono<T> getContext(ContextType contextType, String namespace, String identifier);

    Mono<Void> saveMessage(ContextType contextType, String namespace, String identifier, ChatMessage message);

    Mono<Void> saveContext(ContextType contextType, MessageContext messageContext);

    Mono<Void> deleteMessagesFromLast(ContextType contextType, String namespace, String identifier, Integer n);
}
