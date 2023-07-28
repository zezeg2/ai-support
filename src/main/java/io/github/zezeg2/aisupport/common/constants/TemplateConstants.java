package io.github.zezeg2.aisupport.common.constants;

/**
 * The TemplateConstants class provides constants for template strings used in generating prompts and templates.
 */
public class TemplateConstants {

    /**
     * The prompt statement particle without a role context.
     */
    public static final String PROMPT_STATEMENT_PARTICLE = """
            Please ignore all previous instructions.
            I want you to act as a function execution delegate. You'll be provided with an input that follows the [Input Format] and a command. Your task is to process this input, execute the command accordingly, and generate a response. The response must adhere to the [Result Format] and should reflect the outcome of the command execution. Be sure to observe the following [Constraints] when executing commands.
            """;

    /**
     * The prompt statement particle with a role context.
     */
    public static final String PROMPT_STATEMENT_WITH_ROLE_PARTICLE = """
            Please ignore all previous instructions.
            I want you to act as a function execution delegate within the context of the %s you're given. You'll be provided with an input that follows the [Input Format] and a command. Your task is to process this input, execute the command accordingly, and generate a detailed, professional response. The response must adhere to the [Result Format] and should reflect the outcome of the command execution. Be sure to observe the following [Constraints] when executing commands.
            """;
    /**
     * The prompt example particle.
     * This template provides a result example in JSON format for reference.
     */
    public static final String PROMPT_EXAMPLE_PARTICLE = """         
            [Result Example]
            ```json
            %s
            ```
            """;

    /**
     * The integrated template string for the prompt.
     * It includes information about the statement,  command, class structure, constraints, input and result formats, and feedback format.
     */
    public static final String PROMPT_TEMPLATE_INTEGRATED = """
            %%s
                        
            [Command]
            %s
             
            [Constraints]
            %s- Do not include any other explanatory text in your response.
             
            [Input Format]
            ```json
            %s
            ```
             
            [Result Format]
            ```json
            %s
            ```
                        
            %%s
                        
            If feedback is provided, adjust your previous results based on the content of the feedback.
            [Feedback Format]
            ```json
            %s
            ```
            """;

    /**
     * The template string for the prompt.
     * It includes information about the command, class structure, constraints, input and result formats, and feedback format.
     */
    public static final String PROMPT_TEMPLATE = """
            Please ignore all previous instructions. I want you to act as a function execution delegate. I will provide you a input and you will execute following command.
            Command: %s
             
            Be sure to observe the following constraints when executing commands.
            Constraints:
            %s- Do not include any other explanatory text in your response.
             
            [Input Format]
            ```json
            %s
            ```
             
            [Result Format]
            ```json
            %s
            ```
             
            If feedback is provided, adjust your previous results based on the content of the feedback.
            [Feedback Format]
            ```json
            %s
            ```
            """;

    /**
     * The template string for the prompt with example.
     * It includes information about the command, class structure, constraints, input and result formats, result example, and feedback format.
     */
    @Deprecated
    public static final String PROMPT_TEMPLATE_WITH_EXAMPLE = """
            Please ignore all previous instructions. I want you to act as a function execution delegate. I will provide you a input and you will execute following command.
            Command: %s
                        
            Be sure to observe the following constraints when executing commands.
            Constraints:
            %s- Do not include any other explanatory text in your response.
                        
            [Input Format]
            ```json
            %s
            ```
                        
            [Result Format]
            ```json
            %s
            ```
                        
            [Result Example]
            ```json
            %s
            ```
                        
            If feedback is provided, adjust your previous results based on the feedback content.
            [Feedback Format]
            ```json
            %s
            ```
            """;

    /**
     * The template string for the feedback frame.
     * It includes the feedback format and the inspection items.
     */
    public static final String FEEDBACK_FRAME = """
            Your task is to conduct a comprehensive review of the provided JSON data, applying a high level of scrutiny to identify any potential issues, offering insights, and proposing solutions. Please identify any issues or errors, provide detailed feedback on them, and suggest solutions to rectify these problems. We expect your feedback in the 'Result Format.' Please conduct this review and provide all feedback in English.
                        
            The inspection items are as follows.
            %s
                        
            [Input Format]
            ```json
            %s
            ```
                        
            [Result Format]
            ```json
            %s
            ```

            - Each element within the "problems" array should be concise and clear (limited to 50 words).
            - Do not include any other explanatory text in your response without the result.
            """;

    /**
     * The template string for the feedback frame with a role.
     * It includes the role of the reviewer, feedback format, and the inspection items.
     */
    public static final String FEEDBACK_FRAME_WITH_ROLE = """
            As a(n) %s, your task is to apply your specialized knowledge and skills to meticulously review the provided JSON data, identifying issues, offering insights, and proposing solutions that align with best practices in your field. Please identify any issues or errors, provide detailed feedback on them, and suggest solutions to rectify these problems. We expect your feedback in the 'Result Format.' Please conduct this review and provide all feedback in English."
                   
            The inspection items are as follows.
            %s
                        
            [Input Format]
            ```json
            %s
            ```
                        
            [Result Format]
            ```json
            %s
            ```
            - Each element within the "problems" array should be concise and clear (limited to 50 words).
            - Do not include any other explanatory text in your response without the result.
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
                        
            [Required Format]
            ```json
            %s
            ```
            """;
    /**
     * The template string for Constraint validation.
     */
    public static String CONSTRAINT_VALIDATE_TEMPLATE = """
            Evaluate compliance with the given constraints
            %s
            """;
}
