package io.github.zezeg2.aisupport.ai.config.properties;

import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.ai.model.gpt.GPTModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-supporter.api")
@Data
public class OpenAIProperties {
    private String token = "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";
    private Integer timeout = 60;
    private GPTModel model = GPT3Model.GPT_3_5_TURBO;
}
