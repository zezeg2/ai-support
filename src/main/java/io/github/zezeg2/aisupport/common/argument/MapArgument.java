package io.github.zezeg2.aisupport.common.argument;

import java.util.Map;

/**
 * The MapArgument class represents a map argument with a specific data type.
 *
 * @param <T> The type of data stored in the map argument.
 */
public class MapArgument<T> extends BaseArgument<Map<String, T>> {

    /**
     * Constructs a MapArgument with the provided type, field name, value, description, and wrapping type.
     *
     * @param type        The wrapping type of data stored in the map argument.
     * @param fieldName   The name of the field associated with the map argument.
     * @param value       The value stored in the map argument (a map with keys of type String and values of type T).
     * @param desc        The description of the map argument.
     * @param wrappedType The type of data stored in the map argument.
     */
    public MapArgument(Class<Map<String, T>> type, String fieldName, Map<String, T> value, String desc, Class<?> wrappedType) {
        super(type, fieldName, value, desc, wrappedType);
    }

    /**
     * Gets the Class object representing the type of data stored in the map argument.
     *
     * @return The Class object representing the type of data stored in the map argument.
     */
    @Override
    public Class<?> getType() {
        return wrappedType;
    }

    /**
     * Gets the Class object representing the wrapping type of data stored in the map argument.
     *
     * @return The Class object representing the wrapping type of data stored in the map argument.
     */
    @Override
    public Class<Map<String, T>> getWrapping() {
        return type;
    }

    /**
     * Gets the description of the map argument.
     *
     * @return The description of the map argument.
     */
    @Override
    public String getDesc() {
        return desc;
    }

    /**
     * Gets the value stored in the map argument.
     *
     * @return The value stored in the map argument (a map with keys of type String and values of type T).
     */
    @Override
    public Map<String, T> getValue() {
        return value;
    }

    /**
     * Gets the name of the type of data stored in the map argument (Map<String, T>).
     *
     * @return The name of the type of data stored in the map argument.
     */
    @Override
    public String getTypeName() {
        return "Map<String, " + wrappedType.getSimpleName() + ">";
    }

    /**
     * Gets the String representation of the value stored in the map argument.
     *
     * @return The String representation of the value stored in the map argument.
     */
    @Override
    public String getValueToString() {
        return value.toString();
    }

    /**
     * Gets the name of the field associated with the map argument.
     *
     * @return The name of the field associated with the map argument.
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }
}

