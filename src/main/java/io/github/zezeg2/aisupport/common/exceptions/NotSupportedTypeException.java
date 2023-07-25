package io.github.zezeg2.aisupport.common.exceptions;

/**
 * NotSupportedTypeException is a custom exception class that extends RuntimeException.
 * It is used to indicate that a certain type is not BaseSupportType.
 */
public class NotSupportedTypeException extends RuntimeException {

    /**
     * Constructs a new NotSupportedTypeException with a default error message
     * indicating that the type is not supported.
     */
    public NotSupportedTypeException() {
        super("This is NotSupported Type");
    }
}
