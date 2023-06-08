package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.AISupporter;
import io.github.zezeg2.aisupport.ai.ReactiveAISupporter;
import io.github.zezeg2.aisupport.ai.function.prompt.PromptManager;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactivePromptManager;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactiveSecurityContextPromptManager;
import io.github.zezeg2.aisupport.ai.function.prompt.ReactiveSessionContextPromptManager;
import io.github.zezeg2.aisupport.ai.validator.DefaultExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.ExceptionValidator;
import io.github.zezeg2.aisupport.ai.validator.ReactiveResultValidator;
import io.github.zezeg2.aisupport.ai.validator.ResultValidator;
import io.github.zezeg2.aisupport.ai.validator.chain.ReactiveResultValidatorChain;
import io.github.zezeg2.aisupport.ai.validator.chain.ResultValidatorChain;
import io.github.zezeg2.aisupport.config.properties.ContextProperties;
import io.github.zezeg2.aisupport.config.properties.OpenAIProperties;
import io.github.zezeg2.aisupport.context.reactive.*;
import io.github.zezeg2.aisupport.context.servlet.*;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

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
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "SERVLET")
    public AISupporter aiSupporter(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, PromptManager promptManager, ResultValidatorChain resultValidatorChain, ExceptionValidator exceptionValidator) {
        return new AISupporter(service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactiveAISupporter<ServerWebExchange> reactiveSessionContextAISupporter(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, ReactivePromptManager<ServerWebExchange> promptManager, ReactiveResultValidatorChain<ServerWebExchange> resultValidatorChain, ExceptionValidator exceptionValidator) {
        return new ReactiveAISupporter<>(service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactiveAISupporter<Authentication> reactiveSecurityContextAISupporter(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, ReactivePromptManager<Authentication> promptManager, ReactiveResultValidatorChain<Authentication> resultValidatorChain, ExceptionValidator exceptionValidator) {
        return new ReactiveAISupporter<>(service, mapper, resolver, promptManager, resultValidatorChain, exceptionValidator, openAIProperties);
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIProperties.getToken(), Duration.ofSeconds(openAIProperties.getTimeout()));
    }

    @Bean
    public ResultValidatorChain resultValidatorChain(List<ResultValidator> validatorList) {
        return new ResultValidatorChain(validatorList);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactiveResultValidatorChain<ServerWebExchange> reactiveSessionContextResultValidatorChain(List<ReactiveResultValidator<ServerWebExchange>> validatorList) {
        return new ReactiveResultValidatorChain<>(validatorList);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactiveResultValidatorChain<Authentication> reactiveSecurityContextResultValidatorChain(List<ReactiveResultValidator<Authentication>> validatorList) {
        return new ReactiveResultValidatorChain<>(validatorList);
    }

    @Bean(name = "defaultExceptionValidator")
    public ExceptionValidator exceptionValidator(PromptManager promptManager) {
        return new DefaultExceptionValidator(promptManager, mapper());
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'REDIS' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public PromptContextHolder redisPromptContextHolder(RedisTemplate<String, String> redisTemplate,
                                                        ObjectMapper mapper) {
        return new RedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'MONGO' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public PromptContextHolder mongoPromptContextHolder(MongoTemplate mongoTemplate) {
        return new MongoPromptContextHolder(mongoTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactivePromptContextHolder<ServerWebExchange> reactiveSessionContextRedisPromptContextHolder(ReactiveRedisTemplate<String, String> redisTemplate,
                                                                                                         ObjectMapper mapper) {
        return new ReactiveRedisPromptContextHolder<>(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactivePromptContextHolder<ServerWebExchange> reactiveSessionContextMongoPromptContextHolder(ReactiveMongoTemplate mongoTemplate) {
        return new ReactiveMongoPromptContextHolder<>(mongoTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactivePromptContextHolder<Authentication> reactiveSecurityContextRedisPromptContextHolder(ReactiveRedisTemplate<String, String> redisTemplate,
                                                                                                       ObjectMapper mapper) {
        return new ReactiveRedisPromptContextHolder<>(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactivePromptContextHolder<Authentication> reactiveSecurityContextMongoPromptContextHolder(ReactiveMongoTemplate mongoTemplate) {
        return new ReactiveMongoPromptContextHolder<>(mongoTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(PromptContextHolder.class)
    public PromptContextHolder localPromptContextHolder() {
        return new LocalPromptContextHolder();
    }


    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'SESSION' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public ContextIdentifierProvider sessionIdentifierProvider() {
        return new SessionContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'SECURITY' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public ContextIdentifierProvider securityIdentifierProvider() {
        return new SecurityContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactiveContextIdentifierProvider<ServerWebExchange> reactiveSessionIdentifierProvider() {
        return new ReactiveSessionContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactiveContextIdentifierProvider<Authentication> reactiveSecurityIdentifierProvider() {
        return new ReactiveSecurityContextIdentifierProvider();
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
    public ConstructResolver resolver() {
        return new JAVAConstructResolver();
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "SERVLET")
    public PromptManager promptManager(OpenAiService service, PromptContextHolder promptContextHolder, ContextIdentifierProvider contextIdentifierProvider, ContextProperties contextProperties) {
        return new PromptManager(service, promptContextHolder, contextIdentifierProvider, contextProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactivePromptManager<ServerWebExchange> reactiveSessionContextPromptManager(OpenAiService service, ReactivePromptContextHolder<ServerWebExchange> promptContextHolder, ReactiveSessionContextIdentifierProvider contextIdentifierProvider, ContextProperties contextProperties) {
        return new ReactiveSessionContextPromptManager(service, promptContextHolder, contextIdentifierProvider, contextProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ai-supporter.context.environment", value = "EVENTLOOP")
    public ReactivePromptManager<Authentication> reactiveSecurityContextPromptManager(OpenAiService service, ReactivePromptContextHolder<Authentication> promptContextHolder, ReactiveSecurityContextIdentifierProvider contextIdentifierProvider, ContextProperties contextProperties) {
        return new ReactiveSecurityContextPromptManager(service, promptContextHolder, contextIdentifierProvider, contextProperties);
    }
}
