package io.github.zezeg2.aisupport.common;

/**
 * The TemplateConstants class provides constants for template strings used in generating prompts and templates.
 *
 * @since 1.0
 */
public class TemplateConstants {

    /**
     * The template string for the prompt.
     * It includes information about the command, class structure, constraints, input and result formats, and feedback format.
     */
    public static final String PROMPT_TEMPLATE = """
            You are tasked to act as a function execution delegate. You will be given an input and required to execute the following command.
 
            Command: %s
 
            Be sure to observe the following constraints when executing commands.
            Constraints:
            %s- Only respond with your `return` value. Do not include any other explanatory text in your response.
 
            Input Format:
            ```json
            %s
            ```
 
            Result Format:
            ```json
            %s
            ```
 
            If feedback is provided, adjust your previous results based on the content of the feedback.
            Feedback Format
            ```json
            %s
            ```
            """;

    /**
     * The template string for the prompt with example.
     * It includes information about the command, class structure, constraints, input and result formats, result example, and feedback format.
     */
    public static final String PROMPT_TEMPLATE_WITH_EXAMPLE = """
            I want you to act as a function execution delegate. I will provide you a input and you will execute following command.
            
            Command: %s
            
            Be sure to observe the following constraints when executing commands.
            Constraints:
            %s- Only respond with your `return` value. Do not include any other explanatory text in your response.
            
            Input Format:
            ```json
            %s
            ```
            
            Result Format:
            ```json
            %s
            ```
            
            Result Example:
            ```json
            %s
            ```
            
            If feedback is provided, adjust your previous results based on the feedback content.
            Feedback Format
            ```json
            %s
            ```
            """;

    /**
     * The template string for the feedback frame.
     * It includes the feedback format and the inspection items.
     */
    public static final String FEEDBACK_FRAME = """
            Your task is to inspect the 'assistantOutput' of the provided JSON data. Please provide feedback according to the given `Feedback Format`.
            
            Feedback Format:
            ```json
            %s
            ```
            
            The inspection items are as follows.
            %s
            
            Do not include any other explanatory text in your response apart from the result.
            """;

    /**
     * The template string for JSON validation.
     * It includes steps for validating the JSON string.
     */
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
    /**
     * The template string for Constraint validation.
     */
    public static String CONSTRAINT_VALIDATE_TEMPLATE = """
            Evaluate compliance with the given constraints
            ---
            %s
            ---
            """;
}
