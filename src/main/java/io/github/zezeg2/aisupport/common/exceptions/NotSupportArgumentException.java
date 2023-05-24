package io.github.zezeg2.aisupport.common.exceptions;

public class NotSupportArgumentException extends RuntimeException {
    public NotSupportArgumentException() {
        super("NotSupported Wrapping");
    }

    public NotSupportArgumentException(String message) {
        super(message);
    }
}
