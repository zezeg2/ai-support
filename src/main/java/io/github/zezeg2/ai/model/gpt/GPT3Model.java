package io.github.zezeg2.ai.model.gpt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GPT3Model implements GPTModel {

    GPT_3_5_TURBO("gpt-3.5-turbo", 0.000002),
    GPT_3_5_TURBO_0301("gpt-3.5-turbo-0301", 0.000002),
    TEXT_DAVINCI_003("text-davinci-003", 0.000002),
    TEXT_DAVINCI_002("text-davinci-002", 0.000002);

    private final String value;
    private final double price;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GPT3Model findByValue(String value) {
        return Arrays.stream(values())
                .filter(model -> model.getValue().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

}
