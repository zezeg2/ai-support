package io.github.zezeg2.aisupport.common.exceptions;

public class NotInitiatedContextException extends RuntimeException {
    public NotInitiatedContextException() {
        super("Not Initiated Context Before add user ChatMessage, add system ChatMessage");
    }

    public NotInitiatedContextException(String message) {
        super(message);
    }
}
