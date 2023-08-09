package io.github.zezeg2.aisupport.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.aop.annotation.PubToKafka;
import io.github.zezeg2.aisupport.context.reactive.ReactiveIdentifierProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Aspect
@Slf4j
@Component
@ConditionalOnExpression("'${ai-supporter.kafka-publish.enabled}' == 'true' && '${ai-supporter.context.environment}' == 'reactive'")
public class ReactiveKafkaPublisherAspect {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AdminClient adminClient;

    private final ReactiveIdentifierProvider identifierProvider;

    public ReactiveKafkaPublisherAspect(ObjectMapper mapper, KafkaTemplate<String, Object> kafkaTemplate, AdminClient adminClient, @Autowired(required = false) ReactiveIdentifierProvider identifierProvider) {
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.adminClient = adminClient;
        this.identifierProvider = identifierProvider;
    }

    private void ensureTopicExists(String topicName) {
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
        try {
            describeTopicsResult.topicNameValues().get(topicName).get();
        } catch (InterruptedException | ExecutionException e) {
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            adminClient.createTopics(Collections.singletonList(newTopic));
        }
    }

    @Around("@annotation(pubToKafkaAnnotation)")
    public Object publishToKafkaReactive(ProceedingJoinPoint joinPoint, PubToKafka pubToKafkaAnnotation) throws Throwable {
        ensureTopicExists(pubToKafkaAnnotation.topic());

        Object result = joinPoint.proceed();
        if (result instanceof Flux<?> flux) {
            return flux.flatMap(element -> convertAndSend(pubToKafkaAnnotation, element).thenReturn(element));
        } else if (result instanceof Mono<?> mono) {
            return mono.flatMap(element -> convertAndSend(pubToKafkaAnnotation, element).thenReturn(element));
        }
        return result;
    }

    private Mono<Void> convertAndSend(PubToKafka pubToKafkaAnnotation, Object result) {
        return Mono.defer(() -> {
            try {
                String jsonString = mapper.writeValueAsString(result);
                if (!pubToKafkaAnnotation.key().isEmpty()) {
                    kafkaTemplate.send(pubToKafkaAnnotation.topic(), pubToKafkaAnnotation.key(), jsonString);
                    return Mono.empty();
                } else if (identifierProvider != null) {
                    return identifierProvider.get().flatMap(identifier -> {
                        kafkaTemplate.send(pubToKafkaAnnotation.topic(), identifier, jsonString);
                        return Mono.empty();
                    });
                } else {
                    kafkaTemplate.send(pubToKafkaAnnotation.topic(), jsonString);
                    return Mono.empty();
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize object to JSON for Kafka publishing.", e);
                return Mono.error(e);
            }
        });
    }
}

