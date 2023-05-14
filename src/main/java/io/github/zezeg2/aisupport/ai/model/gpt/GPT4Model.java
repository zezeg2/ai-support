package io.github.zezeg2.aisupport.ai.model.gpt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum GPT4Model implements GPTModel {

    /**
     * More capable than any GPT-3.5 model, able to do more complex tasks, and optimized for chat. Will be updated with our latest model iteration..
     * 8k context
     */
    GPT_4("gpt-4", 0.00003d, 0.00006d),

    /**
     * Snapshot of gpt-4 from March 14th 2023. Unlike gpt-4, this model will not receive updates, and will only be supported for a three month period ending on June 14th 2023.
     * 8k context, but specific snapshot
     */
    GPT_4_0314("gpt-4-0314", 0.00003d, 0.00006d),

    /**
     * Same capabilities as the base gpt-4 mode but with 4x the context length. Will be updated with our latest model iteration.
     * 32k context
     */
    GPT_4_32_K("gpt-4-32k", 0.00006d, 0.00012d),

    /**
     * Snapshot of gpt-4-32 from March 14th 2023. Unlike gpt-4-32k, this model will not receive updates, and will only be supported for a three month period ending on June 14th 2023.
     * 32k context, but specific snapshot
     */
    GPT_4_32_K_0314("gpt-4-32k-0314", 0.00006d, 0.00012d);

    @Getter
    private final String value;

    // price per token for prompt
    private final double requestPrice;
    // price per token for completion
    private final double responsePrice;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GPT4Model findByValue(String value) {
        return Arrays.stream(values())
                .filter(model -> model.getValue().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @Override
    public double getPrice() {
        return 0;
    }

}
