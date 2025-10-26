package com.collabrix.auth.controller;

import com.collabrix.auth.dto.*;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.service.AuthService;
import com.collabrix.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * /auth endpoints:
 * - POST /auth/register
 * - POST /auth/login
 * - POST /auth/refresh
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Register a new user.
     * Flow: Controller -> AuthService -> UserService -> Repository
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for username: {}", request.getUsername());
        User saved = authService.register(request);
        return ResponseEntity.ok(userService.toResponse(saved));
    }

    /**
     * Login: authenticate user and return access & refresh tokens.
     * Flow: Controller -> AuthService -> AuthenticationManager -> JwtTokenProvider / RefreshTokenService.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token string.
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        JwtResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
