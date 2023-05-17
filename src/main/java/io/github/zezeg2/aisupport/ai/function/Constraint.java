package io.github.zezeg2.aisupport.ai.function;

import lombok.Data;

@Data
public final class Constraint {
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
