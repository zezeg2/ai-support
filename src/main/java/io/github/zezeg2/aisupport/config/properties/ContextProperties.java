package io.github.zezeg2.aisupport.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-supporter.context")
@Data
public class ContextProperties {
    private CONTEXT context = CONTEXT.LOCAL;
    private IDENTIFIER identifier = IDENTIFIER.THREAD;

    public enum CONTEXT{
        LOCAL,
        REDIS,
        MONGO
    }

    public enum IDENTIFIER{
        THREAD,
        SESSION,
        AUTHENTICATION
    }
}
