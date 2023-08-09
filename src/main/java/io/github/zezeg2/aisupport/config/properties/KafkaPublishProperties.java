package io.github.zezeg2.aisupport.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-supporter.kafka-publish")
@Data
public class KafkaPublishProperties {
    private boolean enabled = false;
}
