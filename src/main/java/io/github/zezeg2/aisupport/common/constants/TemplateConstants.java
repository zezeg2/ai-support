package io.github.zezeg2.aisupport.common.constants;

/**
 * The TemplateConstants class provides constants for template strings used in generating prompts and templates.
 */
public class TemplateConstants {

    /**
     * The prompt statement particle without a role context.
     */
    public static final String PROMPT_STATEMENT_PARTICLE = """
            Your task is to execute the [Command] and generate a result in the [Result Format], using the input in the [Input Format].
            Please adhere to the "[Constraints]" given below.
            """;

    /**
     * The prompt statement particle with a role context.
     */
    public static final String PROMPT_STATEMENT_WITH_ROLE_PARTICLE = """
            Please consider yourself as a(n) %s. Your task is to execute the [Command] and generate a detailed, professional result in the [Result Format], using the input in the [Input Format].
            Please adhere to the "[Constraints]" given below.
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
    public static final String PROMPT_TEMPLATE_BODY = """
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
            If feedback is provided in the "[Feedback Format]", adjust your previous results based on the content of the feedback.
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
            Your task is to conduct a comprehensive inspection of the provided JSON data and identify issues.
            Perform an inspection solely according to the [Inspection Criteria], identify issues, then propose solutions to rectify these issues.
            After you've completed your assessment, please provide feedback using the [Result Format].
                        
            [Inspection Criteria]
            %s
                        
            [Input Format]
            ```json
            %s
            ```
                        
            [Result Format]
            ```json
            %s
            ```
                        
            - Each element within the "issueList" should be concise and clear.
            - Do not include any other explanatory text in your response without the result.
            """;

    /**
     * The template string for the feedback frame with a role.
     * It includes the role of the reviewer, feedback format, and the inspection items.
     */
    public static final String FEEDBACK_FRAME_WITH_ROLE = """
            As a(n) %s, your task is to apply your specialized knowledge and skills to meticulously inspect the provided JSON data and identify issues.
            Perform an inspection solely according to the [Inspection Criteria], identify issues, then propose solutions to rectify these issues.
            After you've completed your assessment, please provide feedback using the [Result Format].
                   
            [Inspection Criteria]
            %s
                        
            [Input Format]
            ```json
            %s
            ```
                        
            [Result Format]
            ```json
            %s
            ```
                        
            - Each element within the "issueList" should be concise and clear.
            - Do not include any other explanatory text in your response without the result.
            """;

    /**
     * The template string for JSON validation.
     * It includes steps for validating the JSON string.
     */
    public static final String JSON_VALIDATE_TEMPLATE = """
            Your task is to conduct a comprehensive inspection of the provided JSON data and identify issues.
            Conduct an inspection solely for JSON format validation, identify any issues, then propose solutions to rectify these issues.
            After you've completed your assessment, please provide feedback using the [Result Format].
             
            Follow these inspection criteria:
            - Parse-ability: Ensure the provided JSON string is valid and can be parsed without errors.
            - Schema Compliance: Refer to the provided [Class Info], confirm if the JSON string complies with the given schema [Required Format].
            - Incorrectly Escaped Characters: Identify any incorrectly escaped characters within the JSON string and provide a corrected version.
                        
            [Class Info]
            ```java
            %s
            ```
                        
            [Required Format]
            ```json
            %s
            ```
                        
            [Result Format]
            ```json
            %s
            ```
                        
            - Each element within the "issueList" should be concise and clear.
            - Do not include any other explanatory text in your response without the result.
            """;

    /**
     * The template string for Constraint validation.
     */
    public static String CONSTRAINT_VALIDATE_TEMPLATE = """
            Evaluate compliance with the given constraints
            %s
            """;
}
