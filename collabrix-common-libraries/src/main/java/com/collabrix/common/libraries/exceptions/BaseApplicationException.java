package com.collabrix.common.libraries.exceptions;

public abstract class BaseApplicationException extends RuntimeException {
    public BaseApplicationException(String message) {
        super(message);
    }
}
