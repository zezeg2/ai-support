package io.github.zezeg2.aisupport.core.function.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.common.TemplateConstants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    @JsonCreator
    public Prompt(
            @JsonProperty("functionName") String functionName,
            @JsonProperty("purpose") String purpose,
            @JsonProperty("refTypes") String refTypes,
            @JsonProperty("function") String function,
            @JsonProperty("constraints") String constraints,
            @JsonProperty("inputFormat") String inputFormat,
            @JsonProperty("resultFormat") String resultFormat,
            @JsonProperty("feedbackFormat") String feedbackFormat) {

        this.functionName = functionName;
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
