package com.collabrix.auth.service.interfaces;

import com.collabrix.auth.dto.KeycloakLoginRequest;
import com.collabrix.auth.dto.KeycloakLogoutRequest;
import com.collabrix.auth.dto.KeycloakTokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface KeycloakAuthService {
    void logout(KeycloakLogoutRequest request);
    KeycloakTokenResponse login(KeycloakLoginRequest request,HttpServletRequest httpReq);
    KeycloakTokenResponse refreshToken(String refreshToken);
}
