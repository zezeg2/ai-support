package io.github.zezeg2.aisupport.ai.function.prompt;

import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.ai.validator.ResultValidator;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Prompt {
    protected static final String PROMPT_TEMPLATE = """
            You are a function execution delegate. When I present an input, you generate and return the result. If you perform your role well, the `Lambda Function` below will finally run in action.
                        
            The purpose I want to achieve through the function is as follows.
            Purpose: %s
                        
            The class structure is as follows:
            ```java
            %s
            ```
                        
            Here's the main class where you need to execute the lambda function:
            ```java
            %s
            ```
                        
            Please adhere to the following constraints when generating results.
            Constraints:
            - Only respond with your `return` value. Do not include any other explanatory text in your response.
            %s
                        
            Input Format:
            ```json
            %s
            ```
                        
            Result Format:
            ```json
            %s
            ```
                        
            If you receive input in the given `Feedback Format`(FeedbackResponse.class), please respond by reflecting the content of the feedback in the previous results.
            Feedback Format
            ```json
            %s
            ```
            """;
    private final String purpose;
    private final String refTypes;
    private final String function;
    private final String constraints;
    private final String inputFormat;
    private final String resultFormat;
    private final String feedbackFormat;
    private final Map<String, List<ChatMessage>> promptMessageContext;
    private final Map<String, List<ChatMessage>> feedbackAssistantContext;
    private final List<ResultValidator> resultValidators;


    public Prompt(String purpose, String refTypes, String function, String constraints, String inputFormat, String resultFormat, String feedbackFormat, List<ResultValidator> resultValidators) {
        this.purpose = purpose;
        this.refTypes = refTypes;
        this.function = function;
        this.constraints = constraints;
        this.inputFormat = inputFormat;
        this.resultFormat = resultFormat;
        this.feedbackFormat = feedbackFormat;
        this.promptMessageContext = new ConcurrentHashMap<>();
        this.feedbackAssistantContext = new ConcurrentHashMap<>();
        this.resultValidators = resultValidators;

    }

    @Override
    public String toString() {
        return String.format(PROMPT_TEMPLATE, this.purpose, this.refTypes, this.function, this.constraints, this.inputFormat, this.resultFormat, this.feedbackFormat);
    }
}
