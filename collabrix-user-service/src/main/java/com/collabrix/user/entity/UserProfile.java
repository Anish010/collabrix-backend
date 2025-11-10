package com.collabrix.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "profile_completed")
    @Builder.Default
    private Boolean profileCompleted = false;

    @Column(name = "profile_completion_percentage")
    @Builder.Default
    private Integer profileCompletionPercentage = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    // Business Methods

    /**
     * Calculate profile completion percentage
     */
    public void calculateProfileCompletion() {
        int totalFields = 12; // Total trackable fields
        int filledFields = 0;

        filledFields = getFilledFields(filledFields, firstName, lastName, countryCode, contactNo, organization);
        filledFields = getFilledFields(filledFields, avatarUrl, bio, linkedinUrl, githubUrl, twitterUrl);
        if (websiteUrl != null && !websiteUrl.isEmpty()) filledFields++;
        // Username and email are always filled (from registration)
        filledFields += 2;

        this.profileCompletionPercentage = (filledFields * 100) / totalFields;
        this.profileCompleted = this.profileCompletionPercentage >= 80;
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
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Soft delete (deactivate) user
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Reactivate user
     */
    public void activate() {
        this.active = true;
    }
}