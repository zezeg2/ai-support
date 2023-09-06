package io.github.zezeg2.aisupport.common.constants;

/**
 * The TemplateConstants class provides constants for template strings used in generating prompts and templates.
 */
public class TemplateConstants {

    /**
     * The prompt statement particle without a role context.
     */
    public static final String PROMPT_STATEMENT_PARTICLE = """
            Follow the [Command], and using the [Input Format], produce a result in the [Result Format]. You must strictly adhere to the [Constraints].
            """;

    /**
     * The prompt statement particle with a role context.
     */
    public static final String PROMPT_STATEMENT_WITH_ROLE_PARTICLE = """
             Assume the role of a(n) %s. Follow the [Command], and using the [Input Format], produce a result in the [Result Format]. You must strictly adhere to the [Constraints].
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
            %s- Include only the result in the [Result Format] and no other text in your response.
             
            [Input Format]
            ```json
            %s
            ```
             
            [Result Format]
            ```json
            %s
            ```
                        
            %%s
            If feedback is provided using the "[Feedback Format]", please adjust your previous response accordingly.
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
            Task: Inspect the given JSON data based on the specified [Inspection Criteria]. Identify issues, then suggest corrections.
            
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
            
            Note:
            - Responses in "issueList" should be clear and concise.
            - Include only the result in the [Result Format] and no other text in your response.
            """;

    /**
     * The template string for the feedback frame with a role.
     * It includes the role of the reviewer, feedback format, and the inspection items.
     */
    public static final String FEEDBACK_FRAME_WITH_ROLE = """
            Role: %s
            Task: Inspect the given JSON data based on the specified [Inspection Criteria]. Identify issues, then suggest corrections.
            
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
            
            Note:
            - Responses in "issueList" should be clear and concise.
            - Include only the result in the [Result Format] and no other text in your response.
            """;

    /**
     * The template string for JSON validation.
     * It includes steps for validating the JSON string.
     */
    public static final String JSON_VALIDATE_TEMPLATE = """
            Task: Inspect the given JSON data based on the specified [Inspection Criteria]. Identify issues, then suggest corrections.
             
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
                        
            Note:
            - Responses in "issueList" should be clear and concise.
            - Include only the result in the [Result Format] and no other text in your response.
            """;

    /**
     * The template string for Constraint validation.
     */
    public static String CONSTRAINT_VALIDATE_TEMPLATE = """
            Evaluate compliance with the given constraints
            %s
            """;
}
