package io.github.zezeg2.aisupport.common.argument;

import io.github.zezeg2.aisupport.common.type.BaseSupportType;

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

    private String buildFieldName(Class<?> clazz) {
        String inputString = clazz.getSimpleName();
        return inputString.substring(0, 1).toLowerCase() + inputString.substring(1);
    }

    private String buildFieldName(Class<?> wrapper, Class<?> clazz) {
        String inputString = clazz.getSimpleName();
        return inputString.substring(0, 1).toLowerCase() + inputString.substring(1) + wrapper.getSimpleName();
    }

    public <T> ArgumentsFactory addArgument(T value, ArgumentDesc argumentDesc) {
        Objects.requireNonNull(value, "There is no way to know the description of the field");
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();

        if (argumentDesc != null) {
            String keyDesc = argumentDesc.getKeyDesc();
            String valueDesc = argumentDesc.getValueDesc();
            arguments.add(new SingleArgument<>(type, keyDesc.isEmpty() ? buildFieldName(type) : keyDesc, value, valueDesc.isEmpty() ? "" : valueDesc, null));
        } else arguments.add(new SingleArgument<>(type, buildFieldName(type), value, "", null));
        return this;
    }

    public <T extends BaseSupportType> ArgumentsFactory addArgument(T value) {
        return addArgument(value, null);
    }

    public <T> ArgumentsFactory addArgument(List<T> value, ArgumentDesc argumentDesc) {
        Objects.requireNonNull(value, "Value cannot be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("List Argument cannot be empty");
        }
        @SuppressWarnings("unchecked")
        Class<List<T>> listType = (Class<List<T>>) (Class<?>) List.class;
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.get(0).getClass();

        if (argumentDesc != null) {
            String keyDesc = argumentDesc.getKeyDesc();
            String valueDesc = argumentDesc.getValueDesc();
            arguments.add(new ListArgument<>(listType, keyDesc.isEmpty() ? buildFieldName(listType, type) : keyDesc, value, valueDesc.isEmpty() ? "" : valueDesc, null));
        } else arguments.add(new ListArgument<>(listType, buildFieldName(listType, type), value, "", type));
        return this;
    }

    public <T extends BaseSupportType> ArgumentsFactory addArgument(List<T> value) {
        return addArgument(value, null);
    }

    public <T> ArgumentsFactory addArgument(Map<String, T> value, ArgumentDesc argumentDesc) {
        Objects.requireNonNull(value, "Value cannot be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Map Argument cannot be empty");
        }
        @SuppressWarnings("unchecked")
        Class<Map<String, T>> mapType = (Class<Map<String, T>>) (Class<?>) Map.class;
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.values().iterator().next().getClass();

        if (argumentDesc != null) {
            String keyDesc = argumentDesc.getKeyDesc();
            String valueDesc = argumentDesc.getValueDesc();
            arguments.add(new MapArgument<>(mapType, keyDesc.isEmpty() ? buildFieldName(mapType, type) : keyDesc, value, valueDesc.isEmpty() ? "" : valueDesc, null));
        } else arguments.add(new MapArgument<>(mapType, buildFieldName(type), value, "", type));
        return this;
    }

    public <T extends BaseSupportType> ArgumentsFactory addArgument(Map<String, T> value) {
        return addArgument(value, null);
    }

    public ArgumentsFactory addArguments(Object... values) {
        for (Object value : values) {
            addArgument(value, null);
        }
        return this;
    }

    public List<Argument<?>> build() {
        return new ArrayList<>(arguments);
    }
}

