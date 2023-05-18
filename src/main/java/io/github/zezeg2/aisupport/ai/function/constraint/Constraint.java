package io.github.zezeg2.aisupport.ai.function.constraint;

import lombok.Data;

@Data
public class Constraint {
    private final String topic;
    private final String description;

    public Constraint(String topic, String description) {
        this.topic = topic;
        this.description = description;
    }

    public Constraint(String description) {
        this.topic = "";
        this.description = description;
    }

    public String topic() {
        return topic;
    }

    public String description() {
        return description;
    }
}
