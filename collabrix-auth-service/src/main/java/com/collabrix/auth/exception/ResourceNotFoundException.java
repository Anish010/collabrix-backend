package com.collabrix.auth.exception;

/** Thrown when a requested resource cannot be found. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) { super(msg); }
}
