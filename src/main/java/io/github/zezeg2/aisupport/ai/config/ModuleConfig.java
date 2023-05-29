package io.github.zezeg2.aisupport.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.ai.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.context.ContextIdentifierProvider;
import io.github.zezeg2.aisupport.context.LocalPromptContextHolder;
import io.github.zezeg2.aisupport.context.PromptContextHolder;
import io.github.zezeg2.aisupport.context.ThreadNameIdentifierProvider;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ComponentScan(basePackages = "io.github.zezeg2.aisupport.ai")
@EnableConfigurationProperties({ContextProperties.class, OpenAIProperties.class})

public class ModuleConfig {
    private final OpenAIProperties openAIProperties;
    private final ContextProperties contextProperties;

    public ModuleConfig(OpenAIProperties openAIProperties, ContextProperties contextProperties) {
        this.openAIProperties = openAIProperties;
        this.contextProperties = contextProperties;
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIProperties.getToken(), Duration.ofSeconds(openAIProperties.getTimeout()));
    }

    @Bean
    public PromptContextHolder promptContextHolder() {
        if (contextProperties.getContext().equals(ContextProperties.CONTEXT.LOCAL))
            return new LocalPromptContextHolder();
        else {
            return null;
        }
    }

    @Bean
    public ContextIdentifierProvider identifierProvider() {
        if (contextProperties.getIdentifier().equals(ContextProperties.IDENTIFIER.THREAD))
            return new ThreadNameIdentifierProvider();
        else {
            return null;
        }
    }
    @Bean
    public PromptManager promptManager() {
        return new PromptManager(openAiService(), promptContextHolder(), identifierProvider());
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    public BuildFormatUtil formatUtil() {
        return new BuildFormatUtil();
    }

    @Bean
    public ConstructResolver resolver() {
        return new JAVAConstructResolver();
    }
}
