package com.collabrix.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;              // Keycloak user ID
    private String username;
    private String email;
    private Boolean emailVerified;
    private Boolean active;
    private Long createdTimestamp;
    private Set<String> roles;
}