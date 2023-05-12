package com.jbyee.ai;

import java.util.ArrayList;
import java.util.List;

public class ArgumentsFactory {

    private final List<ArgumentRecord> arguments;

    private ArgumentsFactory() {
        arguments = new ArrayList<>();
    }

    public static ArgumentsFactory builder() {
        return new ArgumentsFactory();
    }

    public ArgumentsFactory addArgument(String fieldName, Object value, Class<?> type) {
        arguments.add(new ArgumentRecord(fieldName, value, type));
        return this;
    }

    public List<ArgumentRecord> build() {
        return new ArrayList<>(arguments);
    }
}
