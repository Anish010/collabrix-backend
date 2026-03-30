package com.collabrix.user.controller;

import com.collabrix.common.libraries.dto.UserProfileCreateRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileCreateController {

    private final UserProfileService userProfileService;

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponse> createProfile(
            @RequestBody UserProfileCreateRequest request) {

        log.info("🆕 Creating profile for user {}", request.getKeycloakUserId());
        UserProfileResponse response = userProfileService.createProfile(request);
        return ResponseEntity.ok(response);
    }
}