package io.github.zezeg2.aisupport.ai.model.gpt;

import io.github.zezeg2.aisupport.config.properties.OpenAIProperties.MODEL;

import java.util.HashMap;
import java.util.Map;

public class ModelMapper {

    private static final Map<MODEL, GPTModel> modelMapping;

    static {
        modelMapping = new HashMap<>();
        modelMapping.put(MODEL.GPT_3_5_TURBO, GPT3Model.GPT_3_5_TURBO);
        modelMapping.put(MODEL.GPT_3_5_TURBO_0301, GPT3Model.GPT_3_5_TURBO_0301);
        modelMapping.put(MODEL.TEXT_DAVINCI_003, GPT3Model.TEXT_DAVINCI_003);
        modelMapping.put(MODEL.TEXT_DAVINCI_002, GPT3Model.TEXT_DAVINCI_002);
        modelMapping.put(MODEL.GPT_4, GPT4Model.GPT_4);
        modelMapping.put(MODEL.GPT_4_0314, GPT4Model.GPT_4_0314);
        modelMapping.put(MODEL.GPT_4_32_K, GPT4Model.GPT_4_32_K);
        modelMapping.put(MODEL.GPT_4_32_K_0314, GPT4Model.GPT_4_32_K_0314);
    }

    public static GPTModel map(MODEL model) {
        return modelMapping.get(model);
    }
}

