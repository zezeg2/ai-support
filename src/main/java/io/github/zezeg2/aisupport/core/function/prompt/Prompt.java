package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constants.TemplateConstants;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.util.BuildFormatUtil;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * The Prompt class represents a prompt entity used in the system.
 * It contains information such as the function name, command, reference types, function code, constraints,
 * input format, result format, feedback format, and topP value.
 */
@Data
@Document
@NoArgsConstructor
public class Prompt implements Serializable {

    @Id
    private String functionName;
    private String command;
    private String constraints;
    private String inputFormat;
    private String resultFormat;
    private String feedbackFormat;
    private double topP;

    /**
     * Constructs a new Prompt instance.
     *
     * @param functionName   The name of the function.
     * @param command        The command of the prompt.
     * @param constraints    The constraints of the prompt.
     * @param inputFormat    The format of the input.
     * @param resultFormat   The format of the result.
     * @param feedbackFormat The format of the feedback.
     * @param topP           The topP value.
     */
    @JsonCreator
    public Prompt(@JsonProperty("functionName") String functionName,
                  @JsonProperty("command") String command,
                  @JsonProperty("constraints") String constraints,
                  @JsonProperty("inputFormat") String inputFormat,
                  @JsonProperty("resultFormat") String resultFormat,
                  @JsonProperty("feedbackFormat") String feedbackFormat,
                  @JsonProperty("topP") double topP) {
        this.functionName = functionName;
        this.command = command;
        this.constraints = constraints;
        this.inputFormat = inputFormat;
        this.resultFormat = resultFormat;
        this.feedbackFormat = feedbackFormat;
        this.topP = topP;
    }

    /**
     * Constructs a new Prompt instance with the specified properties and automatically generates
     * format strings for constraints, input arguments, return type, and feedback.
     *
     * @param functionName The name of the function associated with the prompt.
     * @param command      The command of the prompt.
     * @param constraints  The list of Constraint objects representing the constraints of the prompt.
     * @param args         The list of Argument objects representing the input arguments.
     * @param returnType   The Class object representing the return type.
     * @param topP         The topP value associated with the prompt.
     */
    public Prompt(String functionName, String command, List<Constraint> constraints, List<Argument<?>> args, Class<?> returnType, double topP) {
        this.functionName = functionName;
        this.command = command;
        this.constraints = BuildFormatUtil.createConstraintsString(constraints);
        this.inputFormat = BuildFormatUtil.getArgumentsFormatMapString(args);
        this.resultFormat = BuildFormatUtil.getFormatString(returnType);
        this.feedbackFormat = BuildFormatUtil.getFormatString(FeedbackResponse.class);
        this.topP = topP;
    }

    /**
     * Generates the prompt using the template constants.
     *
     * @return The generated prompt string.
     */
    public String generate() {
        return String.format(TemplateConstants.PROMPT_TEMPLATE, this.command, this.constraints, this.inputFormat, this.resultFormat, this.feedbackFormat);
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
            return String.format(TemplateConstants.PROMPT_TEMPLATE_WITH_EXAMPLE, this.command, this.constraints, this.inputFormat, this.resultFormat, exampleString, this.feedbackFormat);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
