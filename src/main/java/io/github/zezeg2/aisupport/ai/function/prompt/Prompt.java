package io.github.zezeg2.aisupport.ai.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Prompt {
    protected static final String PROMPT_TEMPLATE = """
            You are now the following Java Lambda function
            ```java
            %s
                        
            // Purpose: %s
            %s
            ```
            Constraints
            - Only respond with your `return` value. Do not include any other explanatory text in your response.
            %s
            - Input Format : %s
            - Result Format : %s
            """;
    private final String purpose;
    private final String refTypes;
    private final String function;
    private final String constraints;
    private final String inputFormat;
    private final String resultFormat;
    private final Map<String, List<ChatMessage>> promptMessageContext;
    private final Map<String, List<ChatMessage>> feedbackAssistantContext;

    public Prompt(String purpose, String refTypes, String function, String constraints, String inputFormat, String resultFormat) {
        this.purpose = purpose;
        this.refTypes = refTypes;
        this.function = function;
        this.constraints = constraints;
        this.inputFormat = inputFormat;
        this.resultFormat = resultFormat;
        this.promptMessageContext = new ConcurrentHashMap<>();
        this.feedbackAssistantContext = new ConcurrentHashMap<>();
    }

    @Override
    public String toString() {
        return String.format(PROMPT_TEMPLATE, this.function, this.purpose, this.refTypes, this.constraints, this.inputFormat, this.resultFormat);
    }
}
