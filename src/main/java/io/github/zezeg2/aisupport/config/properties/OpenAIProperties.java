package io.github.zezeg2.aisupport.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-supporter.api")
@Data
public class OpenAIProperties {
    private String token = "";
    private Integer timeout = 60;
    private MODEL model = MODEL.GPT_3_5_TURBO;
}
