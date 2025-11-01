package com.collabrix.auth.controller;

import com.collabrix.auth.dto.*;
import com.collabrix.auth.service.AuthService;
import com.collabrix.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controller for authentication and registration endpoints.
 * Handles login, registration, and token refresh.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        JwtResponse jwtResponse = authService.register(registerRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Authenticate a user and return JWT + refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Refresh JWT using a valid refresh token.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        JwtResponse jwtResponse = refreshTokenService.refreshToken(requestRefreshToken);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Logout a user by invalidating their refresh token.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestBody TokenRefreshRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok("User logged out successfully.");
    }
}