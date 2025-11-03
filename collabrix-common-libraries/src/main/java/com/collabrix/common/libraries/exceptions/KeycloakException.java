package com.collabrix.common.libraries.exceptions;
/**
 * Custom exception to handle Keycloak-related authentication and token errors.
 */
public class KeycloakException extends RuntimeException {

    public KeycloakException() {
        super();
    }

    public KeycloakException(String message) {
        super(message);
    }

    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakException(Throwable cause) {
        super(cause);
    }
}
