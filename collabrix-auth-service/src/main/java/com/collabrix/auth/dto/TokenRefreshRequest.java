package com.collabrix.auth.dto;

import lombok.Data;

/**
 * DTO used to request a new JWT using a refresh token.
 */
@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
