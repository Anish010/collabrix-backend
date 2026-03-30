package com.collabrix.auth.mapper;

import com.collabrix.auth.dto.UserRegisterRequest;
import com.collabrix.common.libraries.dto.UserProfileCreateRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserRegisterRequestMapper {

    public static UserProfileCreateRequest toUserProfileRequest(
            String userId, UserRegisterRequest req) {

        return UserProfileCreateRequest.builder()
                .keycloakUserId(userId)
                .username(req.getUsername())
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .countryCode(req.getCountryCode())
                .contactNo(req.getContactNo())
                .organization(req.getOrganization())
                .build();
    }
}
