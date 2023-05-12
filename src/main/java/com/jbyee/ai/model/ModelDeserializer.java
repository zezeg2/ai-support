package com.jbyee.ai.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jbyee.ai.model.gpt.GPT3Model;
import com.jbyee.ai.model.gpt.GPT4Model;
import com.jbyee.ai.model.gpt.GPTModel;

import java.io.IOException;
import java.util.stream.Stream;

public class ModelDeserializer extends StdDeserializer<GPTModel> {

    public ModelDeserializer() {
        super(GPTModel.class);
    }

    @Override
    public GPTModel deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final String value = ctx.readValue(p, String.class);
        return Stream.of(GPT3Model.values(), GPT4Model.values())
                .flatMap(Stream::of)
                .filter(model -> model.getValue().equals(value))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Invalid value for enum: %s".formatted(value)));
    }

}
