package io.github.zezeg2.aisupport.common.exceptions;

public class NotSupportedTypeException extends RuntimeException {
    public NotSupportedTypeException() {
        super("This is NotSupported Type");
    }

    public NotSupportedTypeException(String message) {
        super(message);
    }
}
