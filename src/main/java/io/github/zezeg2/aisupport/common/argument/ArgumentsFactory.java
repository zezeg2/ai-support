package io.github.zezeg2.aisupport.common.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ArgumentsFactory {

    private final List<Argument<?>> arguments;

    private ArgumentsFactory() {
        arguments = new ArrayList<>();
    }

    public static ArgumentsFactory builder() {
        return new ArgumentsFactory();
    }

    public <T> ArgumentsFactory addArgument(T value, String desc) {
        Objects.requireNonNull(value, "Value cannot be null");
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();
        arguments.add(new SingleArgument<>(type, buildFieldName(type), value, desc, null));
        return this;
    }

    public <T> ArgumentsFactory addArgument(List<T> value, String desc) {
        Objects.requireNonNull(value, "Value cannot be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("List Argument cannot be empty");
        }
        @SuppressWarnings("unchecked")
        Class<List<T>> listType = (Class<List<T>>) (Class<?>) List.class;

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.get(0).getClass();
        arguments.add(new ListArgument<>(listType, buildFieldName(type), value, desc, type));
        return this;
    }

    public <T> ArgumentsFactory addArgument(Map<String, T> value, String desc) {
        Objects.requireNonNull(value, "Value cannot be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Map Argument cannot be empty");
        }
        @SuppressWarnings("unchecked")
        Class<Map<String, T>> mapType = (Class<Map<String, T>>) (Class<?>) Map.class;

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.values().iterator().next().getClass();
        arguments.add(new MapArgument<>(mapType, buildFieldName(type), value, desc, type));
        return this;
    }

    public List<Argument<?>> build() {
        return new ArrayList<>(arguments);
    }

    public String buildFieldName(Class<?> clazz) {
        String inputString = clazz.getSimpleName();
        if (inputString.length() == 0) {
            return inputString;
        }
        return inputString.substring(0, 1).toLowerCase() + inputString.substring(1);
    }
}

