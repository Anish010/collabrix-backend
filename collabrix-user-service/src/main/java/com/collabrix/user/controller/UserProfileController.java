package com.collabrix.user.controller;

import com.collabrix.user.dto.UpdateAvatarRequest;
import com.collabrix.user.dto.UpdateProfileRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.dto.UserStatisticsResponse;
import com.collabrix.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for user profile operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Get user profile by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable String userId) {
        log.info("📥 GET /api/v1/users/{}", userId);
        UserProfileResponse response = userProfileService.getProfileById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user profile by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileResponse> getUserByUsername(@PathVariable String username) {
        log.info("📥 GET /api/v1/users/username/{}", username);
        UserProfileResponse response = userProfileService.getProfileByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @RequestAttribute("userId") String userId) {
        log.info("📥 GET /api/v1/users/me");
        UserProfileResponse response = userProfileService.getProfileById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("📥 PUT /api/v1/users/{}", userId);
        UserProfileResponse response = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user avatar
     */
    @PatchMapping("/{userId}/avatar")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserProfileResponse> updateAvatar(
            @PathVariable String userId,
            @Valid @RequestBody UpdateAvatarRequest request) {
        log.info("📥 PATCH /api/v1/users/{}/avatar", userId);
        UserProfileResponse response = userProfileService.updateAvatar(userId, request.getAvatarUrl());
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete user profile (Admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProfile(@PathVariable String userId) {
        log.info("📥 DELETE /api/v1/users/{}", userId);
        userProfileService.deleteProfile(userId);
        return ResponseEntity.ok(Map.of(
                "message", "User profile deleted successfully",
                "userId", userId
        ));
    }

    /**
     * Reactivate user profile (Admin only)
     */
    @PostMapping("/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> reactivateProfile(@PathVariable String userId) {
        log.info("📥 POST /api/v1/users/{}/reactivate", userId);
        UserProfileResponse response = userProfileService.reactivateProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsers(
            @RequestParam(required = false) String q) {
        log.info("📥 GET /api/v1/users/search?q={}", q);
        List<UserProfileResponse> response = userProfileService.searchUsers(q);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileResponse>> getAllActiveUsers() {
        log.info("📥 GET /api/v1/users");
        List<UserProfileResponse> response = userProfileService.getAllActiveUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * Get users by organization
     */
    @GetMapping("/organization/{organization}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileResponse>> getUsersByOrganization(
            @PathVariable String organization) {
        log.info("📥 GET /api/v1/users/organization/{}", organization);
        List<UserProfileResponse> response = userProfileService.getUsersByOrganization(organization);
        return ResponseEntity.ok(response);
    }

    /**
     * Get users with incomplete profiles (Admin only)
     */
    @GetMapping("/incomplete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileResponse>> getUsersWithIncompleteProfiles() {
        log.info("📥 GET /api/v1/users/incomplete");
        List<UserProfileResponse> response = userProfileService.getUsersWithIncompleteProfiles();
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        log.info("📥 GET /api/v1/users/statistics");
        UserStatisticsResponse response = userProfileService.getUserStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Update last login time
     */
    @PostMapping("/{userId}/last-login")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<Map<String, String>> updateLastLogin(@PathVariable String userId) {
        log.info("📥 POST /api/v1/users/{}/last-login", userId);
        userProfileService.updateLastLogin(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Last login updated successfully",
                "userId", userId
        ));
    }
}