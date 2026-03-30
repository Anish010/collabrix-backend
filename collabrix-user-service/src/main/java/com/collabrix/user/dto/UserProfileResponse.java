package com.collabrix.user.dto;

import com.collabrix.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for user profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String keycloakUserId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String contactNo;
    private String organization;
    private String avatarUrl;
    private String bio;
    private String linkedinUrl;
    private String githubUrl;
    private String twitterUrl;
    private String timezone;
    private String websiteUrl;
    private Boolean active;
    private Boolean suspend;
    private Set<String> roles;
    private Integer loginCount;
    private Integer logoutCount;
    private Boolean profileCompleted;
    private Integer profileCompletionPercentage;
    private Instant lastLoginAt;
    private Instant lastLogoutAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}