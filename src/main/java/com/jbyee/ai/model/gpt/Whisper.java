package com.jbyee.ai.model.gpt;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Whisper implements GPTModel {

    WHISPER_1("whisper-1", 0.006d);

    private final String value;

    //price per minute, rounded to the nearest second
    private final double price;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public double getPrice() {
        return price;
    }

}
