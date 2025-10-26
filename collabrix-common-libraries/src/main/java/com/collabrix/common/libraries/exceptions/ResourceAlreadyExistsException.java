package com.collabrix.common.libraries.exceptions;

/** Thrown when a resource already exists (e.g., username/email). */
public class ResourceAlreadyExistsException extends BaseApplicationException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}