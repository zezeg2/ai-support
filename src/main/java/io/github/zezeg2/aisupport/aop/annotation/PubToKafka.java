package io.github.zezeg2.aisupport.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom annotation used to mark methods for publishing messages to a Kafka topic.
 * This annotation should be applied to methods that are responsible for producing
 * messages to Kafka topics.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PubToKafka {

    /**
     * Specifies the name of the Kafka topic to which the message should be published.
     *
     * @return The name of the Kafka topic.
     */
    String topic();

    /**
     * Specifies an optional key to be associated with the Kafka message.
     * If not provided, the default keying mechanism of Kafka will be used.
     *
     * @return The optional key for the Kafka message.
     */
    String key() default "";
}
