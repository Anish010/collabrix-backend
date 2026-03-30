package com.collabrix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Response DTO for updated user profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileResponse {
    private String keycloakUserId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Boolean active;
    private Boolean suspend;
    private String timezone;
}
