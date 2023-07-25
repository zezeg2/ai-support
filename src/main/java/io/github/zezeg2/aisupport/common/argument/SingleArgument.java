package io.github.zezeg2.aisupport.common.argument;

/**
 * The SingleArgument class represents a single argument with a specific data type.
 *
 * @param <T> The type of data stored in the argument.
 */
public class SingleArgument<T> extends BaseArgument<T> {

    /**
     * Constructs a SingleArgument with the provided type, field name, value, description, and wrapping type.
     *
     * @param type        The type of data stored in the argument.
     * @param fieldName   The name of the field associated with the argument.
     * @param value       The value stored in the argument.
     * @param desc        The description of the argument.
     * @param wrappedType The wrapping type of data stored in the argument.
     */
    public SingleArgument(Class<T> type, String fieldName, T value, String desc, Class<?> wrappedType) {
        super(type, fieldName, value, desc, wrappedType);
    }

    /**
     * Gets the Class object representing the type of data stored in the argument.
     *
     * @return The Class object representing the type of data stored in the argument.
     */
    @Override
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets the Class object representing the wrapping type of data stored in the argument.
     *
     * @return Always returns null for a SingleArgument since it doesn't involve a wrapping type.
     */
    @Override
    public Class<T> getWrapping() {
        return null;
    }

    /**
     * Gets the description of the argument.
     *
     * @return The description of the argument.
     */
    @Override
    public String getDesc() {
        return desc;
    }

    /**
     * Gets the value stored in the argument.
     *
     * @return The value stored in the argument.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Gets the name of the type of data stored in the argument.
     *
     * @return The name of the type of data stored in the argument.
     */
    @Override
    public String getTypeName() {
        return type.getSimpleName();
    }

    /**
     * Gets the String representation of the value stored in the argument.
     *
     * @return The String representation of the value stored in the argument.
     */
    @Override
    public String getValueToString() {
        return value.toString();
    }

    /**
     * Gets the name of the field associated with the argument.
     *
     * @return The name of the field associated with the argument.
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }
}

