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

@Data
@Builder
@Document
public class PromptMessages implements Serializable {
    @Id
    private String identifier;
    private String functionName;
    private List<ChatMessage> content;

    @JsonCreator
    public PromptMessages(@JsonProperty("identifier") String identifier,
                          @JsonProperty("functionName") String functionName,
                          @JsonProperty("content") List<ChatMessage> content) {
        this.identifier = identifier;
        this.functionName = functionName;
        this.content = content;
    }
}
