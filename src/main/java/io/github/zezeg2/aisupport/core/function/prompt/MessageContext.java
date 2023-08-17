package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;

import java.util.List;
import java.util.Map;

/**
 * The MessageContext interface represents a context for storing chat messages in a chat-based AI system.
 * It provides methods to get and set the function name, identifier, and list of chat messages.
 */
public interface MessageContext {

    String getId();

    /**
     * Get a sequence that identifies messageContexts with the same namespace and identifier
     *
     * @return sequence of messageContext
     */
    Long getSeq();

    /**
     * Get the function name associated with this message context.
     *
     * @return The function name.
     */
    String getFunctionName();

    /**
     * Get the first input by the user to execute the function
     *
     * @return The user input
     */
    Map<String, Object> getUserInput();

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
     * Get the Model to be used in this context.
     *
     * @return The list of chat messages.
     */
    AIModel getModel();

    Usage getUsage();

    void setId(String id);

    /**
     * Set a sequence that identifies messageContexts with the same namespace and identifier
     */
    void setSeq(Long seq);

    /**
     * Set the function name for this message context.
     *
     * @param functionName The function name to set.
     */
    void setFunctionName(String functionName);

    /**
     * Set the first input by the user to execute the function.
     *
     * @param userInput first input by the user to execute the function
     */
    void setUserInput(Map<String, Object> userInput);

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

    /**
     * Set the Model to be used in this context.
     *
     * @param model model to be used in this context.
     */
    void setModel(AIModel model);

    void setUsage(Usage usage);

    String getNamespace();

    static String getSequenceName(String namespace, String identifier) {
        return (namespace + "_" + identifier + "_context_seq").toLowerCase();
    }
}