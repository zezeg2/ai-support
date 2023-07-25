package io.github.zezeg2.aisupport.common.argument;

/**
 * The Argument interface represents a generic argument that can be used to store various types of data.
 * this class instance used when creating an AIFunction or ReactiveAIFunction object.
 *
 * @param <T> The type of data stored in the argument.
 */
public interface Argument<T> {

    /**
     * Gets the Class object representing the type of data stored in the argument.
     *
     * @return The Class object representing the type of data stored in the argument.
     */
    Class<?> getType();

    /**
     * Gets the Class object representing the wrapping type of data stored in the argument.
     *
     * @return The Class object representing the wrapping type of data stored in the argument.
     */
    Class<T> getWrapping();

    /**
     * Gets the description of the argument.
     *
     * @return The description of the argument.
     */
    String getDesc();

    /**
     * Gets the value stored in the argument as an Object.
     *
     * @return The value stored in the argument as an Object.
     */
    Object getValue();

    /**
     * Gets the name of the type of data stored in the argument.
     *
     * @return The name of the type of data stored in the argument.
     */
    String getTypeName();

    /**
     * Gets the String representation of the value stored in the argument.
     *
     * @return The String representation of the value stored in the argument.
     */
    String getValueToString();

    /**
     * Gets the name of the field associated with the argument.
     *
     * @return The name of the field associated with the argument.
     */
    String getFieldName();
}