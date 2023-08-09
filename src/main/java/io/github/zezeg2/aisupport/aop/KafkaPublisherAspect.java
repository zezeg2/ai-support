package io.github.zezeg2.aisupport.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.aop.annotation.PubToKafka;
import io.github.zezeg2.aisupport.context.IdentifierProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * This class serves as an aspect for publishing messages to a Kafka topic based on the specified conditions.
 * It utilizes Spring AOP and is triggered by methods annotated with {@link PubToKafka}.
 * Messages are sent synchronously to the Kafka topic using the provided {@link KafkaTemplate}.
 */
@Aspect
@Slf4j
@Component
@ConditionalOnExpression("'${ai-supporter.kafka-publish.enabled}' == 'true' && '${ai-supporter.context.environment}' == 'synchronous'")
public class KafkaPublisherAspect {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AdminClient adminClient;
    private final IdentifierProvider identifierProvider;

    /**
     * Constructs a new instance of {@code KafkaPublisherAspect}.
     *
     * @param mapper             The ObjectMapper used for JSON serialization.
     * @param kafkaTemplate      The KafkaTemplate for sending messages to Kafka.
     * @param adminClient        The AdminClient for managing Kafka topics.
     * @param identifierProvider An optional IdentifierProvider for generating message identifiers.
     */
    public KafkaPublisherAspect(ObjectMapper mapper, KafkaTemplate<String, Object> kafkaTemplate,
                                AdminClient adminClient, @Autowired(required = false) IdentifierProvider identifierProvider) {
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
     * A method executed after a method annotated with {@link PubToKafka} returns successfully.
     * It ensures the topic exists and sends the returned result as a message to the Kafka topic.
     *
     * @param joinPoint            The JoinPoint containing information about the method call.
     * @param pubToKafkaAnnotation The {@link PubToKafka} annotation on the method.
     * @param result               The result returned by the annotated method.
     */
    @AfterReturning(pointcut = "@annotation(pubToKafkaAnnotation)", returning = "result")
    public void publishToKafkaSynchronous(JoinPoint joinPoint, PubToKafka pubToKafkaAnnotation, Object result) {
        ensureTopicExists(pubToKafkaAnnotation.topic());
        convertAndSend(pubToKafkaAnnotation, result);
    }

    /**
     * Converts the provided result object to JSON and sends it to the Kafka topic.
     *
     * @param pubToKafkaAnnotation The {@link PubToKafka} annotation on the method.
     * @param result               The result returned by the annotated method.
     */
    private void convertAndSend(PubToKafka pubToKafkaAnnotation, Object result) {
        try {
            String jsonString = mapper.writeValueAsString(result);
            if (!pubToKafkaAnnotation.key().isEmpty())
                kafkaTemplate.send(pubToKafkaAnnotation.topic(), pubToKafkaAnnotation.key(), jsonString);
            else if (identifierProvider != null)
                kafkaTemplate.send(pubToKafkaAnnotation.topic(), identifierProvider.get(), jsonString);
            else
                kafkaTemplate.send(pubToKafkaAnnotation.topic(), jsonString);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON for Kafka publishing.", e);
        }
    }
}
