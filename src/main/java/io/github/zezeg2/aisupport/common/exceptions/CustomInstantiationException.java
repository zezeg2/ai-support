package io.github.zezeg2.aisupport.common.exceptions;

/**
 * CustomInstantiationException is a custom exception class that extends RuntimeException.
 * It is used to indicate errors that occur during the instantiation of custom objects or maps.
 */
public class CustomInstantiationException extends RuntimeException {

    /**
     * Constructs a new CustomInstantiationException with the specified error message and cause.
     *
     * @param message The error message to describe the exception.
     * @param cause   The underlying cause of the exception.
     */
    public CustomInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new CustomInstantiationException with the default error message
     * indicating that an error occurred while generating the description map.
     *
     * @param cause The underlying cause of the exception.
     */
    public CustomInstantiationException(Throwable cause) {
        super("Error occurred while generating description map.", cause);
    }
}
