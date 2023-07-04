package io.github.zezeg2.aisupport.context;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.FeedbackMessages;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
import io.github.zezeg2.aisupport.core.function.prompt.PromptMessages;

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

    /**
     * Retrieves the prompt chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the context.
     * @return The prompt chat messages associated with the namespace and identifier, or null if not found.
     */
    PromptMessages getPromptChatMessages(String namespace, String identifier);

    /**
     * Retrieves the feedback chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the feedback chat messages.
     * @return The feedback chat messages associated with the namespace and identifier, or null if not found.
     */
    FeedbackMessages getFeedbackChatMessages(String namespace, String identifier);

    /**
     * Saves a prompt chat message for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the context.
     * @param message    The chat message to save.
     */
    void savePromptMessages(String namespace, String identifier, ChatMessage message);

    /**
     * Saves updated state of prompt messages
     *
     * @param messages The updated PromptMessages.
     */
    void savePromptMessages(PromptMessages messages);

    /**
     * Saves a feedback chat message for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the feedback chat messages.
     * @param message    The chat message to save.
     */
    void saveFeedbackMessages(String namespace, String identifier, ChatMessage message);

    /**
     * Saves updated state of feedback messages
     *
     * @param messages The updated FeedbackMessages.
     */
    void saveFeedbackMessages(FeedbackMessages messages);

    /**
     * Deletes the last N prompt chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the context.
     * @param n          The number of messages to delete.
     */
    void deleteLastPromptMessage(String namespace, String identifier, Integer n);

    /**
     * Deletes the last N feedback chat messages for the given namespace and identifier.
     *
     * @param namespace  The namespace of the prompt.
     * @param identifier The identifier of the context.
     * @param n          The number of messages to delete.
     */
    void deleteLastFeedbackMessage(String namespace, String identifier, Integer n);
}
