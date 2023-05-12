package com.jbyee.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstraintsFactory {

    private final List<Constraint> constraintList;

    private ConstraintsFactory() {
        constraintList = new ArrayList<>();
    }

    public static ConstraintsFactory builder() {
        return new ConstraintsFactory();
    }

    public ConstraintsFactory addArgument(String topic, String description) {
        constraintList.add(new Constraint(topic, description));
        return this;
    }

    public Optional<List<Constraint>> build() {
        return Optional.ofNullable(constraintList.isEmpty() ? null : new ArrayList<>(constraintList));
    }
}
