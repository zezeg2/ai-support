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
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * This class acts as an aspect for publishing messages to a Kafka topic in a reactive environment.
 * It is triggered by methods annotated with {@link PubToKafka} and supports reactive types such as Flux and Mono.
 * Messages are sent asynchronously to the Kafka topic using the provided {@link ReactiveKafkaProducerTemplate}.
 */
@Aspect
@Slf4j
@Component
@ConditionalOnExpression("'${ai-supporter.kafka-publish.enabled}' == 'true' && '${ai-supporter.context.environment}' == 'reactive'")
public class ReactiveKafkaPublisherAspect {

    private final ObjectMapper mapper;
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;
    private final AdminClient adminClient;
    private final ReactiveIdentifierProvider identifierProvider;

    /**
     * Constructs a new instance of {@code ReactiveKafkaPublisherAspect}.
     *
     * @param mapper             The ObjectMapper used for JSON serialization.
     * @param kafkaTemplate      The ReactiveKafkaProducerTemplate for sending messages to Kafka reactively.
     * @param adminClient        The AdminClient for managing Kafka topics.
     * @param identifierProvider An optional ReactiveIdentifierProvider for generating message identifiers.
     */
    public ReactiveKafkaPublisherAspect(ObjectMapper mapper, ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate,
                                        AdminClient adminClient, @Autowired(required = false) ReactiveIdentifierProvider identifierProvider) {
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.adminClient = adminClient;
        this.identifierProvider = identifierProvider;
    }

    /**
     * Ensures the existence of the specified Kafka topic. If the topic does not exist, it is created.
     *
     * @param topicName The name of the Kafka topic.
     */
    private void ensureTopicExists(String topicName) {
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
        try {
            describeTopicsResult.topicNameValues().get(topicName).get();
        } catch (InterruptedException | ExecutionException e) {
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            adminClient.createTopics(Collections.singletonList(newTopic));
        }
    }

    /**
     * A method executed around a method annotated with {@link PubToKafka}.
     * It ensures the topic exists and asynchronously sends the result as a message to the Kafka topic.
     *
     * @param joinPoint            The ProceedingJoinPoint containing information about the method call.
     * @param pubToKafkaAnnotation The {@link PubToKafka} annotation on the method.
     * @return The result returned by the annotated method.
     * @throws Throwable if an exception occurs during method execution.
     */
    @Around("@annotation(pubToKafkaAnnotation)")
    public Object publishToKafkaReactive(ProceedingJoinPoint joinPoint, PubToKafka pubToKafkaAnnotation) throws Throwable {
        ensureTopicExists(pubToKafkaAnnotation.topic());

        Object result = joinPoint.proceed();
        if (result instanceof Flux<?>) {
            return ((Flux<?>) result).flatMap(element -> convertAndSend(pubToKafkaAnnotation, element).thenReturn(element));
        } else if (result instanceof Mono<?>) {
            return ((Mono<?>) result).flatMap(element -> convertAndSend(pubToKafkaAnnotation, element).thenReturn(element));
        }
        return result;
    }

    /**
     * Converts the provided result object to JSON and sends it to the Kafka topic reactively.
     *
     * @param pubToKafkaAnnotation The {@link PubToKafka} annotation on the method.
     * @param result               The result returned by the annotated method.
     * @return A Mono representing the asynchronous operation.
     */
    private Mono<Void> convertAndSend(PubToKafka pubToKafkaAnnotation, Object result) {
        return Mono.defer(() -> {
            try {
                String jsonString = mapper.writeValueAsString(result);
                if (!pubToKafkaAnnotation.key().isEmpty()) {
                    return kafkaTemplate.send(pubToKafkaAnnotation.topic(), pubToKafkaAnnotation.key(), jsonString).then();
                } else if (identifierProvider != null) {
                    return identifierProvider.get().flatMap(identifier -> kafkaTemplate.send(pubToKafkaAnnotation.topic(), identifier, jsonString).then());
                } else {
                    return kafkaTemplate.send(pubToKafkaAnnotation.topic(), jsonString).then();
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize object to JSON for Kafka publishing.", e);
                return Mono.error(e);
            }
        });
    }
}

