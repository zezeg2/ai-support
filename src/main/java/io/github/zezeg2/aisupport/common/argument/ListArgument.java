package io.github.zezeg2.aisupport.common.argument;

import java.util.List;

/**
 * The ListArgument class represents a list argument with a specific data type.
 *
 * @param <T> The type of data stored in the list argument.
 */
public class ListArgument<T> extends BaseArgument<List<T>> {

    /**
     * Constructs a ListArgument with the provided type, field name, value, description, and wrapping type.
     *
     * @param type        The wrapping type of data stored in the list argument.
     * @param fieldName   The name of the field associated with the list argument.
     * @param value       The value stored in the list argument (a list of elements of type T).
     * @param desc        The description of the list argument.
     * @param wrappedType The type of data stored in the list argument.
     */
    public ListArgument(Class<List<T>> type, String fieldName, List<T> value, String desc, Class<?> wrappedType) {
        super(type, fieldName, value, desc, wrappedType);
    }

    /**
     * Gets the Class object representing the type of data stored in the list argument
     *
     * @return The Class object representing the type of data stored in the list argument.
     */
    @Override
    public Class<?> getType() {
        return wrappedType;
    }

    /**
     * Gets the Class object representing the wrapping type of data stored in the list argument.
     *
     * @return The Class object representing the wrapping type of data stored in the list argument.
     */
    @Override
    public Class<List<T>> getWrapping() {
        return type;
    }

    /**
     * Gets the description of the list argument.
     *
     * @return The description of the list argument.
     */
    @Override
    public String getDesc() {
        return desc;
    }

    /**
     * Gets the value stored in the list argument.
     *
     * @return The value stored in the list argument (a list of elements of type T).
     */
    @Override
    public List<T> getValue() {
        return value;
    }

    /**
     * Gets the name of the type of data stored in the list argument (List of T).
     *
     * @return The name of the type of data stored in the list argument.
     */
    @Override
    public String getTypeName() {
        return "List<" + wrappedType.getSimpleName() + ">";
    }

    /**
     * Gets the String representation of the value stored in the list argument.
     *
     * @return The String representation of the value stored in the list argument.
     */
    @Override
    public String getValueToString() {
        return value.toString();
    }

    /**
     * Gets the name of the field associated with the list argument.
     *
     * @return The name of the field associated with the list argument.
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }
}

