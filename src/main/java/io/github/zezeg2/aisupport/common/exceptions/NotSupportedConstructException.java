package io.github.zezeg2.aisupport.common.exceptions;

public class NotSupportedConstructException extends RuntimeException {
    public NotSupportedConstructException() {
        super("This is NotSupported Construct");
    }

    public NotSupportedConstructException(String message) {
        super(message);
    }
}
