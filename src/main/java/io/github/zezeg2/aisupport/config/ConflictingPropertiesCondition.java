package io.github.zezeg2.aisupport.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConflictingPropertiesCondition implements Condition {
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
