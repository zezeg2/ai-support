package io.github.zezeg2.aisupport.common.constraint;

import lombok.Data;

/**
 * The Constraint class represents a constraint with a topic and a description.
 * Constraints can have an optional topic that provides additional context for the description.
 */
@Data
public class Constraint {
    private final String topic;
    private final String description;

    /**
     * Constructs a Constraint with the specified topic and description.
     *
     * @param topic       The topic of the constraint (optional).
     * @param description The description of the constraint.
     */
    public Constraint(String topic, String description) {
        this.topic = topic;
        this.description = description;
    }

    /**
     * Constructs a Constraint with only the description.
     * The topic will be an empty string in this case.
     *
     * @param description The description of the constraint.
     */
    public Constraint(String description) {
        this.topic = "";
        this.description = description;
    }

    /**
     * Gets the topic of the constraint.
     *
     * @return The topic of the constraint.
     */
    public String topic() {
        return topic;
    }

    /**
     * Gets the description of the constraint.
     *
     * @return The description of the constraint.
     */
    public String description() {
        return description;
    }
}