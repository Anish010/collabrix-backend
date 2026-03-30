package com.collabrix.auth.service;

import com.collabrix.auth.client.UserProfileClient;
import com.collabrix.auth.dto.UserRegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.mapper.UserRegisterRequestMapper;
import com.collabrix.auth.outbox.OutboxEventWriter;
import com.collabrix.auth.service.interfaces.KeycloakUserService;
import com.collabrix.common.libraries.exceptions.KeycloakException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationOrchestratorServiceImpl {

    private final KeycloakUserService keycloakUser;
    private final UserProfileClient profileClient;
    private final OutboxEventWriter outbox;

    public UserResponse registerUser(UserRegisterRequest req) {
        log.info("🧩 Registration start for {}", req.getUsername());

        // Create in Keycloak
        UserResponse authUser = keycloakUser.createUserOnly(req);

        // Create profile in user-service (sync)
        try {
            profileClient.createProfile(
                    UserRegisterRequestMapper.toUserProfileRequest(
                            authUser.getId(), req));
        } catch (Exception e) {
            log.error("❌ Profile creation failed — rolling back Keycloak user {}", authUser.getId());
            keycloakUser.hardDeleteUser(authUser.getId());
            throw new KeycloakException("Registration failed: " + e.getMessage());
        }

        // Outbox event for fanout
        try{
            outbox.writeUserRegisteredEvent(authUser, req);
        } catch (Exception e) {
            log.error("⚠️ Outbox event write failed for user register with id : {}", authUser.getId());
        }

        log.info("🎉 Registration completed {}", authUser.getId());
        return authUser;
    }
}
