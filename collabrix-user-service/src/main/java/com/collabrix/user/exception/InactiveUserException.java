package com.collabrix.user.exception;

/**
 * Exception thrown when attempting operations on inactive users
 */
public class InactiveUserException extends RuntimeException {
    public InactiveUserException(String message) {
        super(message);
    }
}