package com.collabrix.common.libraries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ApiError error;

    // Success without message
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    // Success with message
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    // Failure with ApiError
    public static <T> ApiResponse<T> fail(ApiError error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = error;
        return response;
    }
}
