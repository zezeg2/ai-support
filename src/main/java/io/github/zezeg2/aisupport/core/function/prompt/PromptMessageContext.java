package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * The PromptMessages class represents a collection of prompt messages used in the system.
 * It contains an identifier, function name, and a list of chat messages.
 *
 * @since 1.0
 */
@Data
@Builder
@Document
public class PromptMessageContext implements Serializable, MessageContext {

    @Id
    private String identifier;
    private String functionName;
    private List<ChatMessage> messages;

    /**
     * Constructs a new PromptMessages instance.
     *
     * @param identifier   The identifier of the prompt messages.
     * @param functionName The name of the function.
     * @param messages     The list of chat messages.
     */
    @JsonCreator
    public PromptMessageContext(@JsonProperty("identifier") String identifier,
                                @JsonProperty("functionName") String functionName,
                                @JsonProperty("messages") List<ChatMessage> messages) {
        this.identifier = identifier;
        this.functionName = functionName;
        this.messages = messages;
    }
}
