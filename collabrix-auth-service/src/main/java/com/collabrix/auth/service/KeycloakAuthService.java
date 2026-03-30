package com.collabrix.auth.service;

import com.collabrix.auth.dto.KeycloakLoginRequest;
import com.collabrix.auth.dto.KeycloakTokenResponse;
import com.collabrix.common.libraries.exceptions.KeycloakException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for Keycloak authentication operations (login, token refresh).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthService {

    private final WebClient keycloakWebClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    /**
     * Login user with Keycloak and get JWT tokens
     */
    public KeycloakTokenResponse login(KeycloakLoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());

        try {
            KeycloakTokenResponse response = keycloakWebClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> Mono.error(new KeycloakException("Login failed: Invalid credentials"))
                    )
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();

            log.info("✅ User '{}' logged in successfully", request.getUsername());
            return response;

        } catch (Exception e) {
            log.error("❌ Login failed for user '{}': {}", request.getUsername(), e.getMessage());
            throw new KeycloakException("Login failed: " + e.getMessage());
        }
    }
    /**
     * Refresh access token using refresh token
     */
    public KeycloakTokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            KeycloakTokenResponse response = keycloakWebClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> Mono.error(new KeycloakException("Token refresh failed"))
                    )
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();

            log.info("✅ Token refreshed successfully");
            return response;

        } catch (Exception e) {
            log.error("❌ Token refresh failed: {}", e.getMessage());
            throw new KeycloakException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Logout user by invalidating refresh token
     */
    public void logout(String refreshToken) {
        log.info("Logging out user");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            keycloakWebClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("✅ User logged out successfully");

        } catch (Exception e) {
            log.error("❌ Logout failed: {}", e.getMessage());
            throw new KeycloakException("Logout failed: " + e.getMessage());
        }
    }
}