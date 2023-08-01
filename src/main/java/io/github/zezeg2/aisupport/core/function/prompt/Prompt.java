package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.zezeg2.aisupport.common.argument.Argument;
import io.github.zezeg2.aisupport.common.constraint.Constraint;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.common.util.BuildFormatUtil;
import io.github.zezeg2.aisupport.core.validator.FeedbackResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.zezeg2.aisupport.common.constants.TemplateConstants.*;

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
    private String role;
    private String command;
    private String constraints;
    private String inputFormat;
    private String resultFormat;
    private String feedbackFormat;
    private String classStructureInfo;
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
                  @JsonProperty("role") String role,
                  @JsonProperty("command") String command,
                  @JsonProperty("constraints") String constraints,
                  @JsonProperty("inputFormat") String inputFormat,
                  @JsonProperty("resultFormat") String resultFormat,
                  @JsonProperty("feedbackFormat") String feedbackFormat,
                  @JsonProperty("classStructureInfo") String classStructureInfo,
                  @JsonProperty("topP") double topP) {
        this.functionName = functionName;
        this.role = role;
        this.command = command;
        this.constraints = constraints;
        this.inputFormat = inputFormat;
        this.resultFormat = resultFormat;
        this.feedbackFormat = feedbackFormat;
        this.classStructureInfo = classStructureInfo;
        this.topP = topP;
    }

    /**
     * Constructs a new Prompt instance with the specified properties and automatically generates
     * format strings for constraints, input arguments, return type, and feedback.
     *
     * @param functionName The name of the function associated with the prompt.
     * @param role         The role of the function associated with the prompt.
     * @param command      The command of the prompt.
     * @param constraints  The list of Constraint objects representing the constraints of the prompt.
     * @param args         The list of Argument objects representing the input arguments.
     * @param returnType   The Class object representing the return type.
     * @param topP         The topP value associated with the prompt.
     */
    public Prompt(String functionName, String role, String command, List<Constraint> constraints, List<Argument<?>> args, Class<?> returnType, double topP, ConstructResolver resolver) {
        this.functionName = functionName;
        this.role = role;
        this.command = command;
        this.constraints = BuildFormatUtil.createConstraintsString(constraints);
        this.inputFormat = BuildFormatUtil.getArgumentsFormatMapString(args);
        this.resultFormat = BuildFormatUtil.getFormatString(returnType);
        this.feedbackFormat = BuildFormatUtil.getFormatString(FeedbackResponse.class);
        this.topP = topP;
        Set<Class<?>> classes = args.stream().map(Argument::getType).collect(Collectors.toSet());
        if (returnType != null) classes.add(returnType);
        this.classStructureInfo = resolver.resolve(classes);
    }

    /**
     * Generates the prompt using the template constants.
     *
     * @return The generated prompt string.
     */
    @Deprecated
    public String generate() {
        return generate("");
    }

    /**
     * Generates the prompt with an example using the template constants.
     *
     * @param example The example object.
     * @return The generated prompt string with the example.
     * @throws RuntimeException if there is an error during serialization.
     */
    public String generate(String example) {
        String intermediateOutput = PROMPT_TEMPLATE_BODY.formatted(this.command, this.constraints, this.inputFormat, this.resultFormat, this.feedbackFormat);
        String statement = role.isEmpty() ? PROMPT_STATEMENT_PARTICLE : PROMPT_STATEMENT_WITH_ROLE_PARTICLE.formatted(role);
        return intermediateOutput.formatted(statement, example.isEmpty() ? "" : PROMPT_EXAMPLE_PARTICLE.formatted(example));
    }
}
