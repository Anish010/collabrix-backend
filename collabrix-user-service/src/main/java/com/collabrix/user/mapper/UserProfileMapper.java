package com.collabrix.user.mapper;

import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile user) {

        return UserProfileResponse.builder()
                .keycloakUserId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .countryCode(user.getCountryCode())
                .contactNo(user.getContactNo())
                .organization(user.getOrganization())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .linkedinUrl(user.getLinkedinUrl())
                .githubUrl(user.getGithubUrl())
                .twitterUrl(user.getTwitterUrl())
                .timezone(user.getTimezone())
                .websiteUrl(user.getWebsiteUrl())
                .suspend(user.getSuspend())
                .active(user.getActive())
                .loginCount(user.getLoginCount())
                .logoutCount(user.getLogoutCount())
                .profileCompleted(user.getProfileCompleted())
                .profileCompletionPercentage(user.getProfileCompletionPercentage())
                .lastLoginAt(user.getLastLoginAt())
                .lastLogoutAt(user.getLastLogoutAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}