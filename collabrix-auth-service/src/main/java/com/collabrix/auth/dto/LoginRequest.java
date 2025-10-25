package com.collabrix.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for login endpoint.
 */
@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
