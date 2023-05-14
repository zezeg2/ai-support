package io.github.zezeg2.ai.model.gpt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.zezeg2.ai.model.AIModel;
import io.github.zezeg2.ai.model.ModelDeserializer;


@JsonDeserialize(using = ModelDeserializer.class)
public interface GPTModel extends AIModel {

    /**
     * Возвращает цену за 1 токен.
     */
    double getPrice();

}
