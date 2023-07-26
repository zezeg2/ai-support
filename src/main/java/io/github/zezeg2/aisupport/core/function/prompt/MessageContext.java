package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

/**
 * The MessageContext interface represents a context for storing chat messages in a chat-based AI system.
 * It provides methods to get and set the function name, identifier, and list of chat messages.
 */
public interface MessageContext {

    /**
     * Get the function name associated with this message context.
     *
     * @return The function name.
     */
    String getFunctionName();

    /**
     * Get the identifier of this message context.
     *
     * @return The identifier.
     */
    String getIdentifier();

    /**
     * Get the list of chat messages stored in this context.
     *
     * @return The list of chat messages.
     */
    List<ChatMessage> getMessages();

    /**
     * Set the function name for this message context.
     *
     * @param functionName The function name to set.
     */
    void setFunctionName(String functionName);

    /**
     * Set the identifier for this message context.
     *
     * @param identifier The identifier to set.
     */
    void setIdentifier(String identifier);

    /**
     * Set the list of chat messages for this context.
     *
     * @param messages The list of chat messages to set.
     */
    void setMessages(List<ChatMessage> messages);

    String getNamespace();

    static String getSequenceName(String namespace, String identifier) {
        return (namespace + "_" + identifier + "_context_seq").toLowerCase();
    }
}