package com.collabrix.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending JWT access + refresh tokens to the client.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;  // Usually "Bearer"
    private String username;
    private String email;
    private String role;

}
