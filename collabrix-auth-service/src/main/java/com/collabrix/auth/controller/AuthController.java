package com.collabrix.auth.controller;

import com.collabrix.auth.dto.*;
import com.collabrix.auth.service.RegistrationOrchestratorServiceImpl;
import com.collabrix.auth.service.interfaces.EmailVerificationService;
import com.collabrix.auth.service.interfaces.KeycloakAuthService;
import com.collabrix.auth.service.interfaces.KeycloakUserService;
import com.collabrix.common.libraries.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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

    private final KeycloakAuthService authService;
    private final KeycloakUserService userService;
    private final RegistrationOrchestratorServiceImpl registrationOrchestratorServiceImpl;
    private final EmailVerificationService emailVerificationService;

    /**
     * Register a new user in Keycloak
     * Extended profile fields are published as event for user-service
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        UserResponse user = registrationOrchestratorServiceImpl.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user));
    }

    /**
     * Login user and get JWT tokens from Keycloak
     */
    @PostMapping("/login")
    public ResponseEntity<KeycloakTokenResponse> login(
            @Valid @RequestBody KeycloakLoginRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(authService.login(request, httpRequest));
    }


    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<KeycloakTokenResponse> refresh(
            @RequestBody Map<String, String> req) {

        return ResponseEntity.ok(authService.refreshToken(req.get("refresh_token")));
    }

    /**
     * Logout user and invalidate tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody KeycloakLogoutRequest request) {

        authService.logout(request);
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