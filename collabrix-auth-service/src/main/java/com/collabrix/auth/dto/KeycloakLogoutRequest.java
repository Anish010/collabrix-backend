package com.collabrix.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakLogoutRequest {
    @NotBlank(message = "User id is required")
    private String keycloakUserId;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}