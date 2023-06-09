package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Prompt implements Serializable {
    private final String purpose;
    private final String refTypes;
    private final String function;
    private final String constraints;
    private final String inputFormat;
    private final String resultFormat;
    private final String feedbackFormat;

    @JsonCreator
    public Prompt(
            @JsonProperty("purpose") String purpose,
            @JsonProperty("refTypes") String refTypes,
            @JsonProperty("function") String function,
            @JsonProperty("constraints") String constraints,
            @JsonProperty("inputFormat") String inputFormat,
            @JsonProperty("resultFormat") String resultFormat,
            @JsonProperty("feedbackFormat") String feedbackFormat) {

        this.purpose = purpose;
        this.refTypes = refTypes;
        this.function = function;
        this.constraints = constraints;
        this.inputFormat = inputFormat;
        this.resultFormat = resultFormat;
        this.feedbackFormat = feedbackFormat;
    }

    @Override
    public String toString() {
        return String.format(TemplateConstants.PROMPT_TEMPLATE, this.purpose, this.refTypes, this.function, this.constraints, this.inputFormat, this.resultFormat, this.feedbackFormat);
    }
}
