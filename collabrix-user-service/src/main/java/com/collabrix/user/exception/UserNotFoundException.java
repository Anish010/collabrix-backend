package com.collabrix.user.exception;

/**
 * Exception thrown when user is not found
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}