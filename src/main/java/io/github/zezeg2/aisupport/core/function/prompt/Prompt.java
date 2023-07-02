package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * The Prompt class represents a prompt entity used in the system.
 * It contains information such as the function name, purpose, reference types, function code, constraints,
 * input format, result format, feedback format, and topP value.
 *
 * @since 1.0
 */
@Data
@Document
public class Prompt implements Serializable {

    @Id
    private String functionName;
    private String purpose;
    private String refTypes;
    private String function;
    private String constraints;
    private String inputFormat;
    private String resultFormat;
    private String feedbackFormat;
    private double topP;

    /**
     * Constructs a new Prompt instance.
     *
     * @param functionName   The name of the function.
     * @param purpose        The purpose of the prompt.
     * @param refTypes       The reference types used in the prompt.
     * @param function       The function code.
     * @param constraints    The constraints of the prompt.
     * @param inputFormat    The format of the input.
     * @param resultFormat   The format of the result.
     * @param feedbackFormat The format of the feedback.
     * @param topP           The topP value.
     */
    @JsonCreator
    public Prompt(@JsonProperty("functionName") String functionName,
                  @JsonProperty("purpose") String purpose,
                  @JsonProperty("refTypes") String refTypes,
                  @JsonProperty("function") String function,
                  @JsonProperty("constraints") String constraints,
                  @JsonProperty("inputFormat") String inputFormat,
                  @JsonProperty("resultFormat") String resultFormat,
                  @JsonProperty("feedbackFormat") String feedbackFormat,
                  @JsonProperty("topP") double topP) {
        this.functionName = functionName;
        this.purpose = purpose;
        this.refTypes = refTypes;
        this.function = function;
        this.constraints = constraints;
        this.inputFormat = inputFormat;
        this.resultFormat = resultFormat;
        this.feedbackFormat = feedbackFormat;
        this.topP = topP;
    }

    /**
     * Generates the prompt using the template constants.
     *
     * @return The generated prompt string.
     */
    public String generate() {
        return String.format(TemplateConstants.PROMPT_TEMPLATE, this.purpose, this.refTypes, this.function, this.constraints, this.inputFormat, this.resultFormat, this.feedbackFormat);
    }

    /**
     * Generates the prompt with an example using the template constants.
     *
     * @param mapper  The object mapper to serialize the example.
     * @param example The example object.
     * @return The generated prompt string with the example.
     * @throws RuntimeException if there is an error during serialization.
     */
    public String generate(ObjectMapper mapper, Object example) {
        try {
            String exampleString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(example);
            return String.format(TemplateConstants.PROMPT_TEMPLATE_WITH_EXAMPLE, this.purpose, this.refTypes, this.function, this.constraints, this.inputFormat, this.resultFormat, exampleString, this.feedbackFormat);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
