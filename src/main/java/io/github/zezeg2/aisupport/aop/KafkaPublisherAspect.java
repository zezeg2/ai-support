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

@Aspect
@Slf4j
@Component
@ConditionalOnExpression("'${ai-supporter.kafka-publish.enabled}' == 'true' && '${ai-supporter.context.environment}' == 'synchronous'")
public class KafkaPublisherAspect {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AdminClient adminClient;

    private final IdentifierProvider identifierProvider;

    public KafkaPublisherAspect(ObjectMapper mapper, KafkaTemplate<String, Object> kafkaTemplate, AdminClient adminClient, @Autowired(required = false) IdentifierProvider identifierProvider) {
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

    @AfterReturning(pointcut = "@annotation(pubToKafkaAnnotation)", returning = "result")
    public void publishToKafkaSynchronous(JoinPoint joinPoint, PubToKafka pubToKafkaAnnotation, Object result) {
        ensureTopicExists(pubToKafkaAnnotation.topic());
        convertAndSend(pubToKafkaAnnotation, result);
    }

    private void convertAndSend(PubToKafka pubToKafkaAnnotation, Object result) {
        try {
            String jsonString = mapper.writeValueAsString(result);
            if (!pubToKafkaAnnotation.key().isEmpty())
                kafkaTemplate.send(pubToKafkaAnnotation.topic(), pubToKafkaAnnotation.key(), jsonString);
            else if (identifierProvider != null)
                kafkaTemplate.send(pubToKafkaAnnotation.topic(), identifierProvider.get(), jsonString);
            else kafkaTemplate.send(pubToKafkaAnnotation.topic(), jsonString);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON for Kafka publishing.", e);
        }
    }
}

