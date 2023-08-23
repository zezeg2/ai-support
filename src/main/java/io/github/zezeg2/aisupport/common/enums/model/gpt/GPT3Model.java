package io.github.zezeg2.aisupport.common.enums.model.gpt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * The GPT3Model enum represents different models of the GPT-3.5 language model.
 */
@Getter
@RequiredArgsConstructor
public enum GPT3Model implements GPTModel {

    /**
     * Most capable GPT-3.5 model and optimized for chat at 1/10th the cost of text-davinci-003. Will be updated with our latest model iteration 2 weeks after it is released.
     */
    GPT_3_5_TURBO("gpt-3.5-turbo", 0.0000015d, 0.000002d),

    /**
     * This is a snapshot of GPT-3.5-Turbo as of March 1, 2023. Unlike GPT-3.5-Turbo, this model will not receive updates and will be discontinued 3 months after a new version is released. It can also handle up to 4,096 tokens and has been trained with data up to September 2021.
     */
    GPT_3_5_TURBO_0301("gpt-3.5-turbo-0301", 0.0000015d, 0.000002d),

    /**
     * Same capabilities as the standard gpt-3.5-turbo model but with 4 times the context.
     */
    GPT_3_5_TURBO_16_K("gpt-3.5-turbo-16k", 0.000003d, 0.00004d),

    /**
     * Snapshot of gpt-3.5-turbo from June 13th, 2023 with function calling data. Unlike gpt-3.5-turbo, this model will not receive updates, and will be deprecated 3 months after a new version is released.
     */
    GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613", 0.000003d, 0.00004d),

    /**
     * Snapshot of gpt-3.5-turbo-16k from June 13th, 2023. Unlike gpt-3.5-turbo-16k, this model will not receive updates, and will be deprecated 3 months after a new version is released.
     */
    GPT_3_5_TURBO_16_K_0613("gpt-3.5-turbo-16k-0613", 0.000003d, 0.00004d);

    private final String value;
    private final double requestPrice;
    private final double responsePrice;

    /**
     * Finds the GPT3Model enum constant based on the provided value.
     *
     * @param value The value to search for.
     * @return The corresponding GPT3Model enum constant, or null if not found.
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GPT3Model findByValue(String value) {
        return Arrays.stream(values())
                .filter(model -> model.getValue().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }

    /**
     * Returns the string representation of the GPT3Model enum constant.
     *
     * @return The string representation of the GPT3Model enum constant.
     */
    @Override
    @JsonValue
    public String toString() {
        return value;
    }
}

