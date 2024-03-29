package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The FeedbackMessageContext class represents a concrete implementation of the MessageContext interface
 * for feedback messages in a chat-based AI system.
 *
 * <p>The FeedbackMessageContext class is annotated with various annotations to support serialization and database operations.
 * It implements the Serializable interface to enable object serialization.
 *
 * <p>Use the constructor to create a new FeedbackMessageContext instance with the specified properties.
 */
@Data
@Builder
@Document
public class FeedbackMessageContext implements Serializable, MessageContext {
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
     * The function name associated with this FeedbackMessageContext.
     */
    private String functionName;

    /**
     * The validator name associated with this FeedbackMessageContext.
     */
    private String validatorName;

    /**
     * The first input by the user to execute the function
     */
    private Map<String, Object> userInput;

    /**
     * The list of chat messages stored in this FeedbackMessageContext.
     */
    private List<ChatMessage> messages;
    private AIModel model;
    private Usage usage;

    /**
     * Constructs a new FeedbackMessageContext instance with the specified properties.
     *
     * @param identifier    The identifier of the FeedbackMessageContext.
     * @param functionName  The function name associated with the FeedbackMessageContext.
     * @param validatorName The validator name associated with the FeedbackMessageContext.
     * @param messages      The list of chat messages to be stored in the FeedbackMessageContext.
     */
    @JsonCreator
    public FeedbackMessageContext(@JsonProperty("id") String id,
                                  @JsonProperty("seq") long seq,
                                  @JsonProperty("identifier") String identifier,
                                  @JsonProperty("functionName") String functionName,
                                  @JsonProperty("validatorName") String validatorName,
                                  @JsonProperty("userInput") Map<String, Object> userInput,
                                  @JsonProperty("messages") List<ChatMessage> messages,
                                  @JsonProperty("model") AIModel model,
                                  @JsonProperty("usage") Usage usage) {

        this.id = id;
        this.seq = seq;
        this.identifier = identifier;
        this.functionName = functionName;
        this.validatorName = validatorName;
        this.userInput = userInput;
        this.messages = messages;
        this.model = model;
        this.usage = usage;
    }

    @Override
    public String getNamespace() {
        return functionName + ":" + validatorName;
    }
}
