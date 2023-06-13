package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class PromptMessagesContext {
    @Id
    private String identifier;
    private String functionName;
    private List<ChatMessage> messages;
}
