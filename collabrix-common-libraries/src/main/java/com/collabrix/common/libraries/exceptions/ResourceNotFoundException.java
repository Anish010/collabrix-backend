package com.collabrix.common.libraries.exceptions;

/** Thrown when a requested resource cannot be found. */
public class ResourceNotFoundException extends BaseApplicationException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
