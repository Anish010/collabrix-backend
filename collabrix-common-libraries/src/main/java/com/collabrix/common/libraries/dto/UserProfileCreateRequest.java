package com.collabrix.common.libraries.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileCreateRequest {
    private String keycloakUserId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String contactNo;
    private String organization;
}