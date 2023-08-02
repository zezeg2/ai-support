package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * The PromptMessageContext class represents a concrete implementation of the MessageContext interface.
 * It is used for storing chat messages and related information in a chat-based AI system.
 *
 * <p>The PromptMessageContext class is annotated with various annotations to support serialization and database operations.
 * It implements the Serializable interface to enable object serialization.
 *
 * <p>Use the constructor to create a new PromptMessageContext instance with the specified properties.
 */
@Data
@Builder
@Document
public class PromptMessageContext implements Serializable, MessageContext {
    private String id;
    /**
     * The current value of the sequence.
     */
    private Long seq;

    /**
     * The identifier of this PromptMessageContext.
     */
    private String identifier;
    /**
     * The function name associated with this PromptMessageContext.
     */

    private String functionName;

    /**
     * The list of chat messages stored in this PromptMessageContext.
     */
    private List<ChatMessage> messages;

    /**
     * Constructs a new PromptMessageContext instance with the specified properties.
     *
     * @param identifier   The identifier of the PromptMessageContext.
     * @param functionName The function name associated with the PromptMessageContext.
     * @param messages     The list of chat messages to be stored in the PromptMessageContext.
     */
    @JsonCreator
    public PromptMessageContext(@JsonProperty("id") String id,
                                @JsonProperty("seq") long seq,
                                @JsonProperty("identifier") String identifier,
                                @JsonProperty("functionName") String functionName,
                                @JsonProperty("messages") List<ChatMessage> messages) {
        this.id = id;
        this.seq = seq;
        this.identifier = identifier;
        this.functionName = functionName;
        this.messages = messages;
    }

    @Override
    public String getNamespace() {
        return functionName;
    }
}
