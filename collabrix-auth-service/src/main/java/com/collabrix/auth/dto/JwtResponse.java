package com.collabrix.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response returned on login / refresh.
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
