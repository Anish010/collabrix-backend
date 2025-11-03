package com.collabrix.auth.controller;

import com.collabrix.auth.dto.KeycloakLoginRequest;
import com.collabrix.auth.dto.KeycloakTokenResponse;
import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.service.EmailVerificationService;
import com.collabrix.auth.service.KeycloakAuthService;
import com.collabrix.auth.service.KeycloakUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication endpoints using Keycloak.
 * Refactored to handle ONLY authentication operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakAuthService keycloakAuthService;
    private final KeycloakUserService keycloakUserService;
    private final EmailVerificationService emailVerificationService;

    /**
     * Register a new user in Keycloak
     * Extended profile fields are published as event for user-service
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("🔐 Registration request for user: {}", request.getUsername());
        UserResponse user = keycloakUserService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Login user and get JWT tokens from Keycloak
     */
    @PostMapping("/login")
    public ResponseEntity<KeycloakTokenResponse> login(@Valid @RequestBody KeycloakLoginRequest request) {
        log.info("🔑 Login request for user: {}", request.getUsername());
        KeycloakTokenResponse tokens = keycloakAuthService.login(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<KeycloakTokenResponse> refreshToken(@RequestBody Map<String, String> request) {
        log.info("🔄 Token refresh request");
        String refreshToken = request.get("refresh_token");
        KeycloakTokenResponse tokens = keycloakAuthService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Logout user and invalidate tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> request) {
        log.info("👋 Logout request");
        String refreshToken = request.get("refresh_token");
        keycloakAuthService.logout(refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ============================================
    // Email Verification Endpoints (NEW)
    // ============================================

    /**
     * Send verification email to user
     */
    @PostMapping("/send-verification-email")
    public ResponseEntity<Map<String, String>> sendVerificationEmail(@RequestBody Map<String, String> request) {
        log.info("📧 Send verification email request");
        String userId = request.get("userId");
        emailVerificationService.sendVerificationEmail(userId);
        return ResponseEntity.ok(Map.of("message", "Verification email sent successfully"));
    }

    /**
     * Verify email (manual verification - for testing)
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> request) {
        log.info("✅ Email verification request");
        String userId = request.get("userId");
        emailVerificationService.verifyEmail(userId);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    /**
     * Resend verification email
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        log.info("🔄 Resend verification email request");
        String userId = request.get("userId");
        emailVerificationService.resendVerificationEmail(userId);
        return ResponseEntity.ok(Map.of("message", "Verification email resent successfully"));
    }

    /**
     * Check email verification status
     */
    @GetMapping("/email-verification-status/{userId}")
    public ResponseEntity<Map<String, Boolean>> checkEmailVerificationStatus(@PathVariable String userId) {
        log.info("🔍 Checking email verification status for user: {}", userId);
        boolean isVerified = emailVerificationService.isEmailVerified(userId);
        return ResponseEntity.ok(Map.of("emailVerified", isVerified));
    }
}