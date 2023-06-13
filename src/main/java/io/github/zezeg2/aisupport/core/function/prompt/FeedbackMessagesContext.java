package io.github.zezeg2.aisupport.core.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class FeedbackMessagesContext {
    @Id
    private String identifier;
    private String functionName;
    private String validatorName;
    private List<ChatMessage> messages;
}
