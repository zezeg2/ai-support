package io.github.zezeg2.aisupport.common.argument;

import io.github.zezeg2.aisupport.common.type.BaseSupportType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The ArgumentsFactory class is a utility class for creating and managing a list of Argument objects.
 */
public class ArgumentsFactory {

    /**
     * The list of Argument objects managed by the factory.
     */
    private final List<Argument<?>> arguments;

    /**
     * Private constructor to create an ArgumentsFactory instance.
     */
    private ArgumentsFactory() {
        arguments = new ArrayList<>();
    }

    /**
     * Static method to obtain a new instance of ArgumentsFactory.
     *
     * @return A new ArgumentsFactory instance.
     */
    public static ArgumentsFactory builder() {
        return new ArgumentsFactory();
    }

    /**
     * Builds the name of the field based on the provided class.
     *
     * @param clazz The class to generate the field name from.
     * @return The generated field name.
     */
    private String buildFieldName(Class<?> clazz) {
        String inputString = clazz.getSimpleName();
        return inputString.substring(0, 1).toLowerCase() + inputString.substring(1);
    }

    /**
     * Builds the name of the field based on the provided wrapper and class.
     *
     * @param wrapper The wrapper class.
     * @param clazz   The class to generate the field name from.
     * @return The generated field name.
     */
    private String buildFieldName(Class<?> wrapper, Class<?> clazz) {
        String inputString = clazz.getSimpleName();
        return inputString.substring(0, 1).toLowerCase() + inputString.substring(1) + wrapper.getSimpleName();
    }

    /**
     * Adds a single argument with the provided value and argument description to the factory.
     *
     * @param value        The value of the argument.
     * @param argumentDesc The description of the argument.
     * @param <T>          The type of the argument value.
     * @return The ArgumentsFactory instance with the added argument.
     * @throws NullPointerException If the provided value is null.
     */
    public <T> ArgumentsFactory addArgument(T value, ArgumentDesc argumentDesc) throws NullPointerException {
        Objects.requireNonNull(value, "There is no way to know the description of the field");
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();

        if (argumentDesc != null) {
            String keyDesc = argumentDesc.getKeyDesc();
            String valueDesc = argumentDesc.getValueDesc();
            arguments.add(new SingleArgument<>(type, keyDesc.isEmpty() ? buildFieldName(type) : keyDesc, value, valueDesc.isEmpty() ? "" : valueDesc, null));
        } else {
            arguments.add(new SingleArgument<>(type, buildFieldName(type), value, "", null));
        }
        return this;
    }

    /**
     * Adds a single argument with the provided value to the factory, assuming no argument description is given.
     * This method is primarily used for BaseSupportType objects.
     *
     * @param value The value of the argument.
     * @param <T>   The type of the argument value.
     * @return The ArgumentsFactory instance with the added argument.
     * @see #addArgument(Object, ArgumentDesc)
     */
    public <T extends BaseSupportType> ArgumentsFactory addArgument(T value) {
        return addArgument(value, null);
    }

    /**
     * Adds a list of arguments with the provided values and argument description to the factory.
     *
     * @param value        The list of argument values.
     * @param argumentDesc The description of the arguments.
     * @param <T>          The type of the argument values.
     * @return The ArgumentsFactory instance with the added arguments.
     * @throws NullPointerException If the provided list of values is null or empty.
     */
    public <T> ArgumentsFactory addArgument(List<T> value, ArgumentDesc argumentDesc) throws NullPointerException {
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
        } else {
            arguments.add(new ListArgument<>(listType, buildFieldName(listType, type), value, "", type));
        }
        return this;
    }

    /**
     * Adds a list argument with the provided values to the factory, assuming no argument description is given.
     *
     * @param value The list of argument values.
     * @param <T>   The type of the argument values, extending BaseSupportType.
     * @return The ArgumentsFactory instance with the added argument.
     * @see #addArgument(List, ArgumentDesc)
     */
    public <T extends BaseSupportType> ArgumentsFactory addArgument(List<T> value) {
        return addArgument(value, null);
    }

    /**
     * Adds a map argument with the provided key-value pairs to the factory, along with an optional argument description.
     *
     * @param value        The map of argument values, where each key corresponds to the argument key, and the associated
     *                     value represents the argument value.
     * @param argumentDesc The description of the arguments.
     * @param <T>          The type of the argument values.
     * @return The ArgumentsFactory instance with the added argument.
     * @throws NullPointerException     If the provided map of values is null.
     * @throws IllegalArgumentException If the provided map of values is empty.
     */
    public <T> ArgumentsFactory addArgument(Map<String, T> value, ArgumentDesc argumentDesc) throws NullPointerException, IllegalArgumentException {
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
        } else {
            arguments.add(new MapArgument<>(mapType, buildFieldName(type), value, "", type));
        }
        return this;
    }

    /**
     * Adds a map argument with the provided key-value pairs to the factory, assuming no argument description is given.
     *
     * @param value The map of argument values, where each key corresponds to the argument key, and the associated
     *              value represents the argument value.
     * @param <T>   The type of the argument values, extending BaseSupportType.
     * @return The ArgumentsFactory instance with the added argument.
     * @see #addArgument(Map, ArgumentDesc)
     */
    public <T extends BaseSupportType> ArgumentsFactory addArgument(Map<String, T> value) {
        return addArgument(value, null);
    }


    /**
     * Adds multiple arguments with the provided values to the factory.
     *
     * @param values The values of the arguments.
     * @return The ArgumentsFactory instance with the added arguments.
     * @see #addArgument(Object, ArgumentDesc)
     */
    public ArgumentsFactory addArguments(Object... values) {
        for (Object value : values) {
            addArgument(value, null);
        }
        return this;
    }

    /**
     * Builds and returns the list of Argument objects managed by the factory.
     *
     * @return The list of Argument objects managed by the factory.
     */
    public List<Argument<?>> build() {
        return new ArrayList<>(arguments);
    }
}