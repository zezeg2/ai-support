package io.github.zezeg2.aisupport.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the context and environment settings.
 * These properties define the default values for the context and environment,
 * which can be customized using the "ai-supporter.context" prefix.
 */
@ConfigurationProperties(prefix = "ai-supporter.context")
@Data
public class ContextProperties {

    /**
     * The default context for data storage and retrieval.
     * It is initialized with the LOCAL context by default.
     */
    private ContextHolderType context = ContextHolderType.LOCAL;

    /**
     * The default environment for program execution.
     * It is initialized with the SYNCHRONOUS environment by default.
     */
    private AppEnv environment = AppEnv.SYNCHRONOUS;
}
