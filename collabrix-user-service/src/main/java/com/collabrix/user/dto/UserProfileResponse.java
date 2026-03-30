package com.collabrix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
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
    private String websiteUrl;
    private Boolean active;
    private List<String> roles;
    private Boolean profileCompleted;
    private Integer profileCompletionPercentage;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}