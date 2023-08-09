package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.common.resolver.JavaConstructResolver;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.config.properties.KafkaPublishProperties;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.context.*;
import io.github.zezeg2.aisupport.context.reactive.*;
import io.github.zezeg2.aisupport.core.AISupport;
import io.github.zezeg2.aisupport.core.ReactiveAISupport;
import io.github.zezeg2.aisupport.core.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.core.reactive.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidator;
import io.github.zezeg2.aisupport.core.reactive.validator.ReactiveResultValidatorChain;
import io.github.zezeg2.aisupport.core.validator.ResultValidator;
import io.github.zezeg2.aisupport.core.validator.ResultValidatorChain;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@EnableConfigurationProperties({ContextProperties.class, OpenAIProperties.class, KafkaPublishProperties.class})
@ComponentScan("io.github.zezeg2.aisupport")
public class AISupportAutoConfiguration {
    private final OpenAIProperties openAIProperties;
    private final ContextProperties contextProperties;

    public AISupportAutoConfiguration(OpenAIProperties openAIProperties, ContextProperties contextProperties) {
        this.openAIProperties = openAIProperties;
        this.contextProperties = contextProperties;
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIProperties.getToken(), Duration.ofSeconds(openAIProperties.getTimeout()));
    }

    @Bean
    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        return mapper;
    }

    @Bean
    public ConstructResolver resolver() {
        return new JavaConstructResolver();
    }

    //DEFAULT
    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
    public AISupport defaultAISupport(ObjectMapper mapper, PromptManager promptManager, ResultValidatorChain resultValidateChain, ConstructResolver resolver) {
        return new AISupport(mapper, promptManager, resultValidateChain, resolver, openAIProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
    public PromptManager defaultPromptManager(OpenAiService service, PromptContextHolder context) {
        return new PromptManager(service, context, contextProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "synchronous")
    public ResultValidatorChain defaultResultValidatorChain(List<ResultValidator> validators) {
        return new ResultValidatorChain(validators);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'redis' && '${ai-supporter.context.environment}' == 'synchronous'")
    public PromptContextHolder redisPromptContextHolder(RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        return new RedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'mongo' && '${ai-supporter.context.environment}' == 'synchronous'")
    public MongoPromptContextHolder mongoPromptContextHolder(MongoTemplate mongoTemplate, SequenceGenerator sequenceGenerator) {
        return new MongoPromptContextHolder(mongoTemplate, sequenceGenerator);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'local' && '${ai-supporter.context.environment}' == 'synchronous'")
    public PromptContextHolder localMemoryPromptContextHolder() {
        return new LocalMemoryPromptContextHolder();
    }

    //EVENTLOOP

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "reactive")
    public ReactiveAISupport reactiveAISupport(ObjectMapper mapper, ReactivePromptManager promptManager, ReactiveResultValidatorChain resultValidatorChain, ConstructResolver resolver) {
        return new ReactiveAISupport(mapper, promptManager, resultValidatorChain, resolver, openAIProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "reactive")
    public ReactivePromptManager reactivePromptManager(OpenAiService service, ReactivePromptContextHolder context) {
        return new ReactivePromptManager(service, context, contextProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", havingValue = "reactive")
    public ReactiveResultValidatorChain reactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        return new ReactiveResultValidatorChain(validators);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'redis' && '${ai-supporter.context.environment}' == 'reactive'")
    public ReactivePromptContextHolder reactivePromptContextHolder(ReactiveStringRedisTemplate redisTemplate, ObjectMapper mapper) {
        return new ReactiveRedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'mongo' && '${ai-supporter.context.environment}' == 'reactive'")
    public ReactiveMongoPromptContextHolder reactiveMongoPromptContextHolder(ReactiveMongoTemplate mongoTemplate, ReactiveSequenceGenerator sequenceGenerator) {
        return new ReactiveMongoPromptContextHolder(mongoTemplate, sequenceGenerator);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'local' && '${ai-supporter.context.environment}' == 'reactive'")
    public ReactivePromptContextHolder reactiveLocalMemoryPromptContextHolder() {
        return new ReactiveLocalMemoryPromptContextHolder();
    }
}
