package com.collabrix.user.exception;

/**
 * Exception thrown when duplicate profile is detected
 */
public class DuplicateProfileException extends RuntimeException {
    public DuplicateProfileException(String message) {
        super(message);
    }
}