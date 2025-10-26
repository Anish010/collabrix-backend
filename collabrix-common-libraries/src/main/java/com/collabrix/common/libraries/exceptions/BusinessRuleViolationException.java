package com.collabrix.common.libraries.exceptions;

/**
 * Generic exception for business or service-level validation errors.
 * Can be used for actions that violate business rules, e.g., deleting a system-defined role.
 */
public class BusinessRuleViolationException extends BaseApplicationException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}