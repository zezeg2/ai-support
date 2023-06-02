package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.AISupporter;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.validator.DefaultExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.ResultValidator;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableConfigurationProperties({ContextProperties.class, OpenAIProperties.class})
public class AISupportAutoConfiguration {
    private final OpenAIProperties openAIProperties;

    public AISupportAutoConfiguration(OpenAIProperties openAIProperties) {
        this.openAIProperties = openAIProperties;
    }

    @Bean
    public AISupporter aiSupporter(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, PromptManager promptManager, BuildFormatUtil formatUtil, ResultValidatorChain resultValidatorChain, ExceptionValidator exceptionValidator) {
        return new AISupporter(service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIProperties.getToken(), Duration.ofSeconds(openAIProperties.getTimeout()));
    }

    @Bean
    public ResultValidatorChain resultValidatorChain(List<ResultValidator> validatorList) {
        return new ResultValidatorChain(validatorList);
    }

    @Bean(name = "defaultExceptionValidator")
    public ExceptionValidator exceptionValidator(PromptManager promptManager) {
        return new DefaultExceptionValidator(promptManager, mapper());
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.context", havingValue = "REDIS")
    public PromptContextHolder redisPromptContextHolder(RedisTemplate<String, String> redisTemplate,
                                                        ObjectMapper mapper) {
        return new RedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.context", havingValue = "MONGO")
    public PromptContextHolder mongoPromptContextHolder(MongoTemplate mongoTemplate) {
        return new MongoPromptContextHolder(mongoTemplate);
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
    public static ObjectMapper mapper() {
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
    public PromptManager promptManager(OpenAiService service, PromptContextHolder promptContextHolder, ContextIdentifierProvider contextIdentifierProvider, ContextProperties contextProperties) {
        return new PromptManager(service, promptContextHolder, contextIdentifierProvider, contextProperties);
    }
}
