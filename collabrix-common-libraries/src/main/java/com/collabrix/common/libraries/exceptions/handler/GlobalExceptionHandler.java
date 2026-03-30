package com.collabrix.common.libraries.exceptions.handler;

import com.collabrix.common.libraries.dto.ApiError;
import com.collabrix.common.libraries.dto.ApiResponse;
import com.collabrix.common.libraries.exceptions.BusinessRuleViolationException;
import com.collabrix.common.libraries.exceptions.ResourceAlreadyExistsException;
import com.collabrix.common.libraries.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<Void>> build(
            HttpStatus status,
            String errorCode,
            String message
    ) {
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(errorCode)
                .message(message)
                .build();

        return ResponseEntity.status(status)
                .body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("❗ Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ResourceAlreadyExistsException ex) {
        log.warn("⚠️ Resource conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessViolation(BusinessRuleViolationException ex) {
        log.warn("🚫 Business rule violation: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("💥 Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again later.");
    }
}
