package io.github.zezeg2.aisupport.context.reactive;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;
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

    /**
     * Retrieves the prompt chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the prompt chat messages.
     * @return A Mono emitting the prompt chat messages associated with the namespace and identifier, or null if not found.
     */
    Mono<PromptMessages> getPromptChatMessages(String namespace, String identifier);

    /**
     * Retrieves the feedback chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the feedback chat messages.
     * @return A Mono emitting the feedback chat messages associated with the namespace and identifier, or null if not found.
     */
    Mono<FeedbackMessages> getFeedbackChatMessages(String namespace, String identifier);

    /**
     * Saves a prompt chat message for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the prompt chat messages.
     * @param message    The chat message to save.
     * @return A Mono representing the completion of the save operation.
     */
    Mono<Void> savePromptMessages(String namespace, String identifier, ChatMessage message);

    Mono<Void> savePromptMessages(PromptMessages messages);

    /**
     * Saves a feedback chat message for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the feedback chat messages.
     * @param message    The chat message to save.
     * @return A Mono representing the completion of the save operation.
     */
    Mono<Void> saveFeedbackMessages(String namespace, String identifier, ChatMessage message);

    Mono<Void> saveFeedbackMessages(FeedbackMessages messages);

    /**
     * Deletes the last N prompt chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the prompt chat messages.
     * @param n          The number of messages to delete.
     * @return A Mono representing the completion of the delete operation.
     */
    Mono<Void> deleteLastPromptMessage(String namespace, String identifier, Integer n);

    /**
     * Deletes the last N feedback chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the feedback chat messages.
     * @param n          The number of messages to delete.
     * @return A Mono representing the completion of the delete operation.
     */
    Mono<Void> deleteLastFeedbackMessage(String namespace, String identifier, Integer n);
}
