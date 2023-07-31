package io.github.zezeg2.aisupport.common.constants;

/**
 * The TemplateConstants class provides constants for template strings used in generating prompts and templates.
 */
public class TemplateConstants {

    /**
     * The prompt statement particle without a role context.
     */
    public static final String PROMPT_STATEMENT_PARTICLE = """
            I want you to act as a function execution delegate. I will provide you with input data that follows the format "[Input Format]". Your task is to execute the [Command], and generate a result in the format of "[Result Format]".
            Be sure to observe the following "[Constraints]" when executing commands.
            """;

    /**
     * The prompt statement particle with a role context.
     */
    public static final String PROMPT_STATEMENT_WITH_ROLE_PARTICLE = """
            I want you to act as a function execution delegate. Please imagine yourself as %s. I will provide you with input data that follows the format "[Input Format]". Your task is to execute the "[Command]", and generate a detailed, professional result in the format of "[Result Format]".
            Be sure to observe the following "[Constraints]" when executing commands.
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
            If feedback in the format of [Feedback Format] is provided, adjust your previous results based on the content of the feedback.
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
            Your task is to conduct a comprehensive examination of the provided JSON data and identify any issues.
            Apply a high level of scrutiny to identify any problems or discrepancies, particularly in the context of the [Inspection Items], and propose solutions to rectify these issues.
            Once you've completed your assessment, please provide a feedback result in the format of "[Result Format]".
                        
            [Inspection Items]
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
            As a(n) %s, your task is to apply your specialized knowledge and skills to meticulously examine the provided JSON data and identify any issues.
            Apply a high level of scrutiny to identify any problems or discrepancies, particularly in the context of the [Inspection Items], and propose solutions to rectify these issues.
            Once you've completed your assessment, please provide a feedback result in the format of "[Result Format]".
                   
            [Inspection Items]
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
            Your task is to comprehensively review the provided JSON data.
            Make sure to identify any issues or errors, provide detailed feedback on them, and suggest solutions to rectify these issues.
            Once you've completed your assessment, please provide a feedback result in the format of "[Result Format]".
             
            Follow these inspection criteria:
            1. Parse-ability: Ensure the provided JSON string is valid and can be parsed without errors. If there's an error, provide its details and location.
            2. Schema Compliance: Refer to the provided [Class Info], confirm if the JSON string complies with the given schema ([Required Format]). If it doesn't, specify the discrepancy.
            3. Handling Incorrectly Escaped Characters: Identify any incorrectly escaped characters within the JSON string and provide a corrected version.
            4. Replacement of None or NaN Values: Replace any 'None' or 'NaN' values in the JSON string with 'null' to facilitate correct parsing. If any such values are found, indicate their locations.
                        
            [Required Format]
            ```json
            %s
            ```
                        
            [Class Info]
            ```java
            %s
            ```
                        
            [Result Format]
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
