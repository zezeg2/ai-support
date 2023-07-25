package io.github.zezeg2.aisupport.common.argument;

/**
 * The BaseArgument class is an abstract class representing a generic argument with a type, field name, value, and description.
 *
 * @param <T> The type of data stored in the argument.
 */
public abstract class BaseArgument<T> implements Argument<T> {

    /**
     * The Class object representing the wrapping type of data stored in the argument.
     */
    protected final Class<T> type;

    /**
     * The name of the field associated with the argument.
     */
    protected final String fieldName;

    /**
     * The value stored in the argument.
     */
    protected final T value;

    /**
     * The description of the argument.
     */
    protected final String desc;

    /**
     * The Class object representing the type of data stored in the argument.
     */
    protected final Class<?> wrappedType;

    /**
     * Constructs a BaseArgument with the provided type, field name, value, description, and wrapping type.
     *
     * @param type        The type of data stored in the argument.
     * @param fieldName   The name of the field associated with the argument.
     * @param value       The value stored in the argument.
     * @param desc        The description of the argument.
     * @param wrappedType The wrapping type of data stored in the argument.
     */
    public BaseArgument(Class<T> type, String fieldName, T value, String desc, Class<?> wrappedType) {
        this.type = type;
        this.fieldName = fieldName;
        this.value = value;
        this.desc = desc;
        this.wrappedType = wrappedType;
    }
}