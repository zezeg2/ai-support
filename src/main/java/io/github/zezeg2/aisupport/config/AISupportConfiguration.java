package io.github.zezeg2.aisupport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.common.resolver.ConstructResolver;
import io.github.zezeg2.aisupport.common.resolver.JAVAConstructResolver;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
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
        return new JAVAConstructResolver();
    }

    //SERVLET
    @Bean
    public DefaultAISupport defaultAISupport(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, DefaultPromptManager promptManager, DefaultResultValidatorChain resultValdateChain) {
        return new DefaultAISupport(service, mapper, resolver, promptManager, resultValdateChain, openAIProperties);
    }

    @Bean
    public DefaultPromptManager defaultPromptManager(OpenAiService service, PromptContextHolder context, ContextIdentifierProvider identifierProvider) {
        return new DefaultPromptManager(service, context, identifierProvider, contextProperties);
    }

    @Bean
    public DefaultResultValidatorChain defaultResultValidatorChain(List<DefaultResultValidator> validators) {
        return new DefaultResultValidatorChain(validators);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'REDIS' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public PromptContextHolder redisPromptContextHolder(RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        return new RedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'LOCAL' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public PromptContextHolder localMemoryPromptContextHolder() {
        return new LocalMemoryPromptContextHolder();
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'THREAD' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public ContextIdentifierProvider threadContextIdentifierProvider() {
        return new ThreadContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'SESSION' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public ContextIdentifierProvider sessionContextIdentifierProvider() {
        return new SessionContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'AUTHENTICATION' && '${ai-supporter.context.environment}' == 'SERVLET'")
    public ContextIdentifierProvider authenticationContextIdentifierProvider() {
        return new AuthenticationContextIdentifierProvider();
    }

    //EVENTLOOP

    @Bean
    public ReactiveAISupport reactiveAISupport(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, ReactivePromptManager promptManager, ReactiveResultValidatorChain resultValdateChain) {
        return new ReactiveAISupport(service, mapper, resolver, promptManager, resultValdateChain, openAIProperties);
    }

    @Bean
    public ReactivePromptManager reactivePromptManager(OpenAiService service, ReactivePromptContextHolder context, ReactiveContextIdentifierProvider identifierProvider) {
        return new ReactivePromptManager(service, context, identifierProvider, contextProperties);
    }

    @Bean
    public ReactiveResultValidatorChain reactiveResultValidatorChain(List<ReactiveResultValidator> validators) {
        return new ReactiveResultValidatorChain(validators);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'REDIS' && '${ai-supporter.context.environment}' == 'EVENTLOOP'")
    public ReactivePromptContextHolder reactivePromptContextHolder(ReactiveStringRedisTemplate redisTemplate, ObjectMapper mapper) {
        return new ReactiveRedisPromptContextHolder(redisTemplate, mapper);
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.context}' == 'LOCAL' && '${ai-supporter.context.environment}' == 'EVENTLOOP'")
    public ReactivePromptContextHolder reactiveLocalMemoryPromptContextHolder() {
        return new ReactiveLocalMemoryPromptContextHolder();
    }


    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'SESSION' && '${ai-supporter.context.environment}' == 'EVENTLOOP'")
    public ReactiveContextIdentifierProvider reactiveSessionContextIdentifierProvider() {
        return new ReactiveSessionContextIdentifierProvider();
    }

    @Bean
    @ConditionalOnExpression("'${ai-supporter.context.identifier}' == 'AUTHENTICATION' && '${ai-supporter.context.environment}' == 'EVENTLOOP'")
    public ReactiveContextIdentifierProvider reactiveAuthenticationContextIdentifierProvider() {
        return new ReactiveAuthenticationContextIdentifierProvider();
    }
}
