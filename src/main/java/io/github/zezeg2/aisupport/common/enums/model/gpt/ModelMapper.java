package io.github.zezeg2.aisupport.common.enums.model.gpt;

import io.github.zezeg2.aisupport.config.properties.Model;

import java.util.HashMap;
import java.util.Map;

public class ModelMapper {

    private static final Map<Model, GPTModel> modelMapping;

    static {
        modelMapping = new HashMap<>();
        modelMapping.put(Model.GPT_3_5_TURBO, GPT3Model.GPT_3_5_TURBO);
        modelMapping.put(Model.GPT_3_5_TURBO_0301, GPT3Model.GPT_3_5_TURBO_0301);
        modelMapping.put(Model.GPT_3_5_TURBO_0613, GPT3Model.GPT_3_5_TURBO_0613);
        modelMapping.put(Model.GPT_3_5_TURBO_16_K, GPT3Model.GPT_3_5_TURBO_16_K);
        modelMapping.put(Model.GPT_3_5_TURBO_16_K_0613, GPT3Model.GPT_3_5_TURBO_16_K_0613);
        modelMapping.put(Model.GPT_4, GPT4Model.GPT_4);
        modelMapping.put(Model.GPT_4_0314, GPT4Model.GPT_4_0314);
        modelMapping.put(Model.GPT_4_32_K, GPT4Model.GPT_4_32_K);
        modelMapping.put(Model.GPT_4_32_K_0314, GPT4Model.GPT_4_32_K_0314);
        modelMapping.put(Model.GPT_4_0613, GPT4Model.GPT_4_0613);
        modelMapping.put(Model.GPT_4_32_K_0613, GPT4Model.GPT_4_32_K_0613);
    }

    public static GPTModel map(Model model) {
        return modelMapping.get(model);
    }
}