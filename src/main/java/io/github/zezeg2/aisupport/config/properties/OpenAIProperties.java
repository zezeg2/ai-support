package io.github.zezeg2.aisupport.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-supporter.api")
@Data
public class OpenAIProperties {
    private String token = "";
    private Integer timeout = 60;
    private MODEL model = MODEL.GPT_3_5_TURBO;

    public enum MODEL{
        GPT_3_5_TURBO,
        GPT_3_5_TURBO_0301,
        TEXT_DAVINCI_003,
        TEXT_DAVINCI_002,
        GPT_4,
        GPT_4_0314,
        GPT_4_32_K,
        GPT_4_32_K_0314;
    }
}
