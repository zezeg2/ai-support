package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.common.resolver.JAVAConstructResolver;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({ContextProperties.class, OpenAIProperties.class})
public class AISupportAutoConfiguration {
    private final OpenAIProperties openAIProperties;

    public AISupportAutoConfiguration(OpenAIProperties openAIProperties) {
        this.openAIProperties = openAIProperties;
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIProperties.getToken(), Duration.ofSeconds(openAIProperties.getTimeout()));
    }

    @Bean
    public static ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    public ConstructResolver resolver() {
        return new JAVAConstructResolver();
    }
}
