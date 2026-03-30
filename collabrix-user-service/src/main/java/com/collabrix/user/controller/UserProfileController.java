package com.collabrix.user.controller;

import com.collabrix.common.libraries.dto.ApiResponse;
import com.collabrix.user.dto.UpdateAvatarRequest;
import com.collabrix.user.dto.UpdateProfileRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String userId) {
        log.info("📥 GET /api/v1/users/{}", userId);
        UserProfileResponse profile = userProfileService.getProfileById(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }


    /**
     * Get user profile by username
     */
    @GetMapping("/username")
    public ResponseEntity<UserProfileResponse> getUserByUsername(@RequestParam String username) {
        log.info("📥 GET /api/v1/users/username/{}", username);
        UserProfileResponse response = userProfileService.getProfileByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @RequestAttribute("userId") String userId) {
        log.info("📥 GET /api/v1/users/me");
        UserProfileResponse profile = userProfileService.getProfileById(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Update user profile
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("📥 PUT /api/v1/users/{}", userId);
        UserProfileResponse profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile));
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
     * Soft delete user profile
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable String userId) {
        log.info("📥 DELETE /api/v1/users/{}", userId);
        userProfileService.deleteProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Profile deleted", null)
        );
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
     * Get user profile completion statistics
     */
    @GetMapping("/me/profile-completion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> getMyProfileCompletion(
            @RequestAttribute("userId") String userId) {
        log.info("📥 GET /api/v1/users/me/profile-completion");
        Integer percent = userProfileService.getProfileCompletionPercentage(userId);
        return ResponseEntity.ok(ApiResponse.success(percent));
    }

}