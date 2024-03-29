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
import java.util.concurrent.CopyOnWriteArrayList;

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
     * The first input by the user to execute the function
     */
    private Map<String, Object> userInput;

    /**
     * The list of chat messages stored in this PromptMessageContext.
     */
    private List<ChatMessage> messages;
    private AIModel model;
    private Usage usage;
    @Builder.Default
    private List<FeedbackMessageContext> feedbackMessageContexts = new CopyOnWriteArrayList<>();

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
                                @JsonProperty("userInput") Map<String, Object> userInput,
                                @JsonProperty("messages") List<ChatMessage> messages,
                                @JsonProperty("model") AIModel model,
                                @JsonProperty("usage") Usage usage,
                                @JsonProperty("feedbackMessageContexts") List<FeedbackMessageContext> feedbackMessageContexts) {
        this.id = id;
        this.seq = seq;
        this.identifier = identifier;
        this.functionName = functionName;
        this.userInput = userInput;
        this.messages = messages;
        this.model = model;
        this.usage = usage;
        this.feedbackMessageContexts = feedbackMessageContexts;
    }

    @Override
    public String getNamespace() {
        return functionName;
    }
}
