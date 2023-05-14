package io.github.zezeg2.aisupport.ai.function;

import java.util.ArrayList;
import java.util.List;

public class ConstraintsFactory {

    private final List<Constraint> constraintList;

    private ConstraintsFactory() {
        constraintList = new ArrayList<>();
    }

    public static ConstraintsFactory builder() {
        return new ConstraintsFactory();
    }

    public ConstraintsFactory addConstraint(String topic, String description) {
        constraintList.add(new Constraint(topic, description));
        return this;
    }

    public List<Constraint> build() {
        return new ArrayList<>(constraintList);
    }
}
