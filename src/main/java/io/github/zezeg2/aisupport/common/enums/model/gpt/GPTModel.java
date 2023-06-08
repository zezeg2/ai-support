package io.github.zezeg2.aisupport.common.enums.model.gpt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.enums.model.ModelDeserializer;


@JsonDeserialize(using = ModelDeserializer.class)
public interface GPTModel extends AIModel {
    double getPrice();
}
