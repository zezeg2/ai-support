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
 * The FeedbackMessages class represents a collection of feedback messages used in the system.
 * It contains an identifier, function name, validator name, and a list of chat messages.
 *
 * @since 1.0
 */
@Data
@Builder
@Document
public class FeedbackMessages implements Serializable {

    @Id
    private String identifier;
    private String functionName;
    private String validatorName;
    private List<ChatMessage> content;

    /**
     * Constructs a new FeedbackMessages instance.
     *
     * @param identifier    The identifier of the feedback messages.
     * @param functionName  The name of the function.
     * @param validatorName The name of the validator.
     * @param content       The list of chat messages.
     */
    @JsonCreator
    public FeedbackMessages(@JsonProperty("identifier") String identifier,
                            @JsonProperty("functionName") String functionName,
                            @JsonProperty("validatorName") String validatorName,
                            @JsonProperty("content") List<ChatMessage> content) {
        this.identifier = identifier;
        this.functionName = functionName;
        this.validatorName = validatorName;
        this.content = content;
    }
}

