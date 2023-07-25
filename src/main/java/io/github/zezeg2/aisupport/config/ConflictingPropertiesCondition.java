package io.github.zezeg2.aisupport.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * A condition implementation that checks for conflicting properties in the context environment.
 * This condition is used to determine if there is a conflict between the "ai-supporter.context.identifier"
 * and "ai-supporter.context.environment" properties.
 *
 * <p>The condition evaluates the values of the two properties and returns true if there is no conflict,
 * or false if both properties are present and have specific values that indicate a conflict.
 *
 * <p>Implementations of this interface should override the "matches" method to provide custom condition logic.
 * The "matches" method is called with the current application's context and the annotated type metadata
 * to perform the condition evaluation.
 *
 * <p>The condition logic in this implementation checks if both properties are present and have specific
 * values ("THREAD" for "ai-supporter.context.identifier" and "EVENTLOOP" for "ai-supporter.context.environment").
 * If both properties have these specific values, the condition returns false, indicating a conflict.
 */
public class ConflictingPropertiesCondition implements Condition {

    /**
     * Check if there is a conflict between the "ai-supporter.context.identifier" and "ai-supporter.context.environment" properties.
     *
     * <p>The "matches" method retrieves the values of the conflicting properties from the provided
     * {@link ConditionContext} and evaluates them based on the predefined logic.
     *
     * @param context  the condition context for the current application's environment and resources.
     * @param metadata metadata of the annotated type being checked for the condition.
     * @return {@code true} if there is no conflict between the properties, {@code false} if there is a conflict.
     * @throws NullPointerException if {@code context} or {@code metadata} is {@code null}.
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Retrieve the values of the conflicting properties
        String identifier = context.getEnvironment().getProperty("ai-supporter.context.identifier");
        String environment = context.getEnvironment().getProperty("ai-supporter.context.environment");

        // Define the condition logic based on the properties
        // For example, if both properties are present and have specific values, return false to indicate a conflict
        return !(identifier != null && identifier.equalsIgnoreCase("THREAD")
                && environment != null && environment.equalsIgnoreCase("EVENTLOOP"));
    }
}
