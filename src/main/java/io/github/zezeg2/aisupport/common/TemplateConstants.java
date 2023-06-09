package io.github.zezeg2.aisupport.common;

public class TemplateConstants {
    public static final String FUNCTION_TEMPLATE = """
            @FunctionalInterface
            public interface FC {
                String %s(%s);
            }
                        
            public class Main {
                public static void main(String[] args) {
                    FC fc = (%s) -> {
                        return [RESULT] //TODO: [RESULT] is JsonString of `%s`
                    };
                }
            }
            """;
    public static final String PROMPT_TEMPLATE = """
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
    public static final String FEEDBACK_FRAME = """
            You are tasked with inspecting the provided Json and please provide feedback according to the given `Feedback Format`
                        
            Feedback Format:
            ```json
            %s
            ```
                            
            The inspection items are as follows.
            %s
                        
                        
            Do not include any other explanatory text in your response other than result
            """;

    public static final String JSON_VALIDATE_TEMPLATE = """
            1. Ensure Parse-ability: Check that the JSON string is valid and can be properly parsed as `Required Format`.
            2. Verify schema compliance: The JSON string should comply with a given schema, omitting any objects or fields mentioned in the schema but not found in the JSON.
            3. Escape double quotes: Within JSON string values, double quotes should be correctly escaped to ensure JSON validity.
            4. Handle incorrectly escaped characters: Check for and handle any incorrectly escaped characters within the JSON string.
            5. Replace None or NaN values: Any 'None' or 'NaN' values in the JSON string should be replaced with 'null' to facilitate correct parsing.
            6. Parse the JSON: Parse the cleaned, schema-compliant JSON.
                    
            Required Format:
            ```json
            %s
            ```
            """;
}
