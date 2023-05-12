package com.jbyee.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArgumentsFactory {

    private final List<Argument> arguments;

    private ArgumentsFactory() {
        arguments = new ArrayList<>();
    }

    public static ArgumentsFactory builder() {
        return new ArgumentsFactory();
    }

    public ArgumentsFactory addArgument(String fieldName, Object value, Class<?> type) {
        arguments.add(new Argument(fieldName, value, type));
        return this;
    }

    public Optional<List<Argument>> build() {
        return Optional.ofNullable(arguments.isEmpty() ? null : new ArrayList<>(arguments));
    }
}
