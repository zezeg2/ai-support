package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.common.resolver.JavaConstructResolver;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.context.*;
import io.github.zezeg2.aisupport.context.reactive.*;
import io.github.zezeg2.aisupport.core.DefaultAISupport;
import io.github.zezeg2.aisupport.core.ReactiveAISupport;
import io.github.zezeg2.aisupport.core.function.prompt.DefaultPromptManager;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidator;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import io.github.zezeg2.aisupport.core.validator.DefaultResultValidator;
import io.github.zezeg2.aisupport.core.validator.DefaultResultValidatorChain;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;

@Configuration
@Conditional(ConflictingPropertiesCondition.class)
@EnableConfigurationProperties({ContextProperties.class, OpenAIProperties.class})
public class AISupportConfiguration {
    private final OpenAIProperties openAIProperties;
    private final ContextProperties contextProperties;

    public AISupportConfiguration(OpenAIProperties openAIProperties, ContextProperties contextProperties) {
        this.openAIProperties = openAIProperties;
        this.contextProperties = contextProperties;
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
        return new JavaConstructResolver();
    }

    //SERVLET
    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
    public DefaultAISupport defaultAISupport(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, DefaultPromptManager promptManager, DefaultResultValidatorChain resultValidateChain) {
        return new DefaultAISupport(service, mapper, resolver, promptManager, resultValidateChain, openAIProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
    public DefaultPromptManager defaultPromptManager(OpenAiService service, PromptContextHolder context) {
        return new DefaultPromptManager(service, context, contextProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
    public DefaultResultValidatorChain defaultResultValidatorChain(List<DefaultResultValidator> validators) {
        return new DefaultResultValidatorChain(validators);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'redis' && '${ai-supporter.context.environment}' == 'synchronous'")
    public PromptContextHolder redisPromptContextHolder(RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        return new RedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'mongo' && '${ai-supporter.context.environment}' == 'synchronous'")
    public MongoPromptContextHolder mongoPromptContextHolder(MongoTemplate mongoTemplate) {
        return new MongoPromptContextHolder(mongoTemplate);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'local' && '${ai-supporter.context.environment}' == 'synchronous'")
    public PromptContextHolder localMemoryPromptContextHolder() {
        return new LocalMemoryPromptContextHolder();
    }

    //EVENTLOOP

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
    public ReactiveAISupport reactiveAISupport(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, ReactivePromptManager promptManager, ReactiveResultValidatorChain resultValidatorChain) {
        return new ReactiveAISupport(service, mapper, resolver, promptManager, resultValidatorChain, openAIProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
    public ReactivePromptManager reactivePromptManager(OpenAiService service, ReactivePromptContextHolder context) {
        return new ReactivePromptManager(service, context, contextProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "eventloop")
    public ReactiveResultValidatorChain reactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        return new ReactiveResultValidatorChain(validators);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'redis' && '${ai-supporter.context.environment}' == 'eventloop'")
    public ReactivePromptContextHolder reactivePromptContextHolder(ReactiveStringRedisTemplate redisTemplate, ObjectMapper mapper) {
        return new ReactiveRedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'mongo' && '${ai-supporter.context.environment}' == 'eventloop'")
    public ReactiveMongoPromptContextHolder reactiveMongoPromptContextHolder(ReactiveMongoTemplate mongoTemplate) {
        return new ReactiveMongoPromptContextHolder(mongoTemplate);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'local' && '${ai-supporter.context.environment}' == 'eventloop'")
    public ReactivePromptContextHolder reactiveLocalMemoryPromptContextHolder() {
        return new ReactiveLocalMemoryPromptContextHolder();
    }
}
