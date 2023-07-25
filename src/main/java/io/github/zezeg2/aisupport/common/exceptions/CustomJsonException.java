package io.github.zezeg2.aisupport.common.exceptions;

/**
 * CustomJsonException is a custom exception class that extends RuntimeException.
 * It is used to indicate errors related to JSON processing and serialization.
 */
public class CustomJsonException extends RuntimeException {

    /**
     * Constructs a new CustomJsonException with the specified error message and cause.
     *
     * @param message The error message to describe the exception.
     * @param cause   The underlying cause of the exception.
     */
    public CustomJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new CustomJsonException with a default error message
     * indicating a failure to convert a map to JSON.
     *
     * @param cause The underlying cause of the exception.
     */
    public CustomJsonException(Throwable cause) {
        super("Failed to convert map to JSON", cause);
    }
}

