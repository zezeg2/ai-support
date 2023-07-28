package io.github.zezeg2.aisupport.common.constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * The ConstraintsFactory class is used to create and manage a list of constraints.
 */
public class ConstraintsFactory {

    private final List<Constraint> constraints;

    /**
     * Constructs a ConstraintsFactory to create and manage constraints.
     */
    private ConstraintsFactory() {
        constraints = new ArrayList<>();
    }

    /**
     * Creates a new instance of ConstraintsFactory.
     *
     * @return A new ConstraintsFactory instance.
     */
    public static ConstraintsFactory builder() {
        return new ConstraintsFactory();
    }

    /**
     * Adds a constraint with a topic and description to the list.
     *
     * @param topic       The topic of the constraint (optional).
     * @param description The description of the constraint.
     * @return The ConstraintsFactory instance with the new constraint added.
     */
    public ConstraintsFactory addConstraint(String topic, String description) {
        constraints.add(new Constraint(topic, description));
        return this;
    }

    /**
     * Adds a constraint with only the description to the list.
     * The topic will be an empty string in this case.
     *
     * @param description The description of the constraint.
     * @return The ConstraintsFactory instance with the new constraint added.
     */
    public ConstraintsFactory addConstraint(String description) {
        constraints.add(new Constraint(description));
        return this;
    }

    /**
     * Builds and returns the list of constraints.
     *
     * @return The list of constraints.
     */
    public List<Constraint> build() {
        return new ArrayList<>(constraints);
    }
}
