package com.collabrix.common.libraries.exceptions.handler;

import com.collabrix.common.libraries.dto.ApiErrorResponse;
import com.collabrix.common.libraries.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${service.name:unknown-service}")
    private String serviceName;

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        serviceName
                ),
                status
        );
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ResourceAlreadyExistsException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessViolation(BusinessRuleViolationException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseAppException(BaseApplicationException ex) {
        log.error("Application exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    }
}
