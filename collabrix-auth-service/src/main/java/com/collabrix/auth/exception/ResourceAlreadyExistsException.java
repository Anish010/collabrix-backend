package com.collabrix.auth.exception;

/** Thrown when a resource already exists (e.g., username/email). */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String msg) { super(msg); }
}
