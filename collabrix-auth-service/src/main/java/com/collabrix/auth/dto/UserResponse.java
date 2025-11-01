package com.collabrix.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

/**
 * What we return to clients about a user (not password).
 */
@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String contactNo;
    private String organization;
    private boolean active;
    private Instant createdAt;
    private Set<String> roles;
}
