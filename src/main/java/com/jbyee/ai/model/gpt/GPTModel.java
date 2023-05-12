package com.jbyee.ai.model.gpt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.jbyee.ai.model.AIModel;
import com.jbyee.ai.model.ModelDeserializer;


@JsonDeserialize(using = ModelDeserializer.class)
public interface GPTModel extends AIModel {

    /**
     * Возвращает цену за 1 токен.
     */
    double getPrice();

}
