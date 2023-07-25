package io.github.zezeg2.aisupport.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the OpenAI API settings.
 * These properties define the default values for the API token, timeout,
 * and the model to be used, which can be customized using the "ai-supporter.api" prefix.
 */
@ConfigurationProperties(prefix = "ai-supporter.api")
@Data
public class OpenAIProperties {

    /**
     * The API token used for authentication with the OpenAI service.
     * It is initialized as an empty string by default.
     */
    private String token = "";

    /**
     * The timeout value in seconds for API requests.
     * It is initialized with a default value of 60 seconds.
     */
    private Integer timeout = 60;

    /**
     * The default model to be used for generating text.
     * It is initialized with the GPT_3_5_TURBO model by default.
     */
    private MODEL model = MODEL.GPT_3_5_TURBO;
}
