package io.github.zezeg2.aisupport.ai.model.gpt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.zezeg2.aisupport.ai.model.AIModel;
import io.github.zezeg2.aisupport.ai.model.ModelDeserializer;


@JsonDeserialize(using = ModelDeserializer.class)
public interface GPTModel extends AIModel {
    double getPrice();
}
