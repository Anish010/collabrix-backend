package com.collabrix.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User Profile Entity - Stores extended user information
 * ID matches Keycloak user ID for consistency
 */
@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_active", columnList = "active")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserProfile {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 255)
    private String id; //Same as Keycloak user ID

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name",nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "country_code",nullable = false, length = 10)
    private String countryCode;

    @Column(name = "contact_no",nullable = false, length = 20)
    private String contactNo;

    @Column(name = "organization", length = 255)
    private String organization;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "twitter_url", length = 255)
    private String twitterUrl;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "suspend", nullable = false)
    @Builder.Default
    private Boolean suspend = false;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "timezone", nullable = false)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "profile_completed")
    @Builder.Default
    private Boolean profileCompleted = false;

    @Column(name = "profile_completion_percentage")
    @Builder.Default
    private Integer profileCompletionPercentage = 0;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_logout_at")
    private Instant lastLogoutAt;

    private Integer loginCount;
    private Integer logoutCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Set<String> roles;

    /**
     * Calculate profile completion percentage
     */
    public void calculateProfileCompletion() {
        int filled = 0;
        int total = 10;

        if (firstName != null) filled++;
        if (lastName != null) filled++;
        if (countryCode != null) filled++;
        if (contactNo != null) filled++;
        if (organization != null) filled++;
        if (avatarUrl != null) filled++;
        if (bio != null) filled++;
        if (linkedinUrl != null) filled++;
        if (githubUrl != null) filled++;
        if (websiteUrl != null) filled++;

        this.profileCompletionPercentage = (filled * 100) / total;
        this.profileCompleted = profileCompletionPercentage >= 80;
    }

    private int getFilledFields(int filledFields, String firstName, String lastName, String countryCode, String contactNo, String organization) {
        if (firstName != null && !firstName.isEmpty()) filledFields++;
        if (lastName != null && !lastName.isEmpty()) filledFields++;
        if (countryCode != null && !countryCode.isEmpty()) filledFields++;
        if (contactNo != null && !contactNo.isEmpty()) filledFields++;
        if (organization != null && !organization.isEmpty()) filledFields++;
        return filledFields;
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginAt = Instant.now();
    }

    /**
     * Soft delete (deactivate) user
     */
    public void deactivate() {
        this.active = false;
    }

    public void onLogin() {
        this.lastLoginAt = Instant.now();
        this.loginCount++;
        this.active = true;
    }

    public void onLogout() {
        this.lastLogoutAt = Instant.now();
        this.logoutCount++;
        this.active = false;
    }

    /**
     * Reactivate user
     */
    public void activate() {
        this.active = true;
    }
}