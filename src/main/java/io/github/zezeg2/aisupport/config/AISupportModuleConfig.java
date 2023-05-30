package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.context.*;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Configuration
@ComponentScan(basePackages = "io.github.zezeg2.aisupport")
@EnableConfigurationProperties({ContextProperties.class, OpenAIProperties.class})
public class AISupportModuleConfig {
    private final OpenAIProperties openAIProperties;
    private final ContextProperties contextProperties;

    public AISupportModuleConfig(OpenAIProperties openAIProperties, ContextProperties contextProperties) {
        this.openAIProperties = openAIProperties;
        this.contextProperties = contextProperties;
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIProperties.getToken(), Duration.ofSeconds(openAIProperties.getTimeout()));
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.context", havingValue = "REDIS")
    public PromptContextHolder redisPromptContextHolder(RedisTemplate<String, String> redisTemplate,
                                                        ContextIdentifierProvider identifierProvider,
                                                        ObjectMapper mapper) {
        return new RedisPromptContextHolder(redisTemplate, identifierProvider, mapper);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.context", havingValue = "MONGO")
    public PromptContextHolder mongoPromptContextHolder(MongoTemplate mongoTemplate,
                                                        ContextIdentifierProvider  identifierProvider) {
        return new MongoPromptContextHolder(mongoTemplate, identifierProvider);
    }

    @Bean
    @ConditionalOnMissingBean(PromptContextHolder.class)
    public PromptContextHolder localPromptContextHolder() {
        return new LocalPromptContextHolder();
    }


    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.identifier", havingValue = "SESSION")
    public ContextIdentifierProvider sessionIdentifierProvider() {
        return new SessionContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.identifier", havingValue = "AUTHENTICATION")
    public ContextIdentifierProvider authenticationIdentifierProvider() {
        return new AuthenticationContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnMissingBean(ContextIdentifierProvider.class)
    public ContextIdentifierProvider threadNameIdentifierProvider() {
        return new ThreadNameIdentifierProvider();
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

    @Bean
    public PromptManager promptManager(OpenAiService service, PromptContextHolder promptContextHolder ,ContextIdentifierProvider contextIdentifierProvider){
        return new PromptManager(service, promptContextHolder, contextIdentifierProvider);
    }
}
