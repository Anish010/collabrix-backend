package com.collabrix.auth.service;

import com.collabrix.auth.dto.*;
import com.collabrix.auth.outbox.OutboxEventWriter;
import com.collabrix.auth.service.interfaces.KeycloakAuthService;
import com.collabrix.common.libraries.exceptions.KeycloakException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthServiceImpl implements KeycloakAuthService {

    private final WebClient keycloakWebClient;
    private final OutboxEventWriter outboxEventWriter;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    /**
     * Login user with Keycloak and get JWT tokens
     */
    @Transactional
    public KeycloakTokenResponse login(KeycloakLoginRequest request, HttpServletRequest httpReq) {
        log.info("🔐 Attempting login for user: {}", request.getEmail());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", request.getEmail()); // Keycloak uses 'username' not 'email'
        formData.add("password", request.getPassword());

        try {
            // Call Keycloak token endpoint
            KeycloakTokenResponse response = keycloakWebClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("❌ Keycloak login error: {}", errorBody);
                                        return Mono.error(new KeycloakException("Login failed: Invalid credentials"));
                                    })
                    )
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null) {
                throw new KeycloakException("Login failed: No token received");
            }

            // Extract user ID from JWT token
            String userId = extractUserIdFromToken(response.getAccessToken());
            response.setKeycloakUserId(userId); // Set extracted user ID in response

            log.info("✅ Extracted userId: {} for email: {}", userId, request.getEmail());

            // Write outbox event (in same transaction)
            try {
                log.info("📦 Writing outbox event for user login: {}", userId);

                outboxEventWriter.writeUserLoginEvent(
                        userId,
                        request.getEmail(),
                        httpReq.getRemoteAddr(),
                        httpReq.getHeader("User-Agent")
                );

                log.info("✅ Outbox event written successfully for user: {}", userId);
            } catch (Exception e) {
                log.error("⚠️ Failed to write outbox event for login: {}", e.getMessage(), e);
                // Continue - don't fail login if outbox write1 fails
            }

            log.info("✅ User '{}' logged in successfully", request.getEmail());
            return response;

        } catch (KeycloakException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected login error: {}", e.getMessage(), e);
            throw new KeycloakException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Logout user by invalidating refresh token
     */
    @Transactional
    public void logout(KeycloakLogoutRequest request) {
        log.info("🚪 Logging out user: {}", request.getKeycloakUserId());

        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new KeycloakException("Refresh token is required for logout");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", request.getRefreshToken());

        try {
            // Call Keycloak logout endpoint
            keycloakWebClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("❌ Keycloak logout error: {}", errorBody);
                                        return Mono.error(new KeycloakException("Logout failed: " + errorBody));
                                    })
                    )
                    .toBodilessEntity()
                    .block();

            log.info("✅ Keycloak logout successful for user: {}", request.getKeycloakUserId());

            // Write outbox event
            try {
                log.info("📦 Writing outbox event for user logout: {}", request.getKeycloakUserId());

                outboxEventWriter.writeUserLogoutEvent(request.getKeycloakUserId());

                log.info("✅ Outbox event written successfully for logout");
            } catch (Exception e) {
                log.error("⚠️ Failed to write outbox event for logout: {}", e.getMessage(), e);
            }

        } catch (KeycloakException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected logout error: {}", e.getMessage(), e);
            throw new KeycloakException("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public KeycloakTokenResponse refreshToken(String refreshToken) {
        log.info("🔄 Refreshing access token");

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

            if (response != null && response.getAccessToken() != null) {
                String userId = extractUserIdFromToken(response.getAccessToken());
                response.setKeycloakUserId(userId);
            }

            log.info("✅ Token refreshed successfully");
            return response;

        } catch (Exception e) {
            log.error("❌ Token refresh failed: {}", e.getMessage());
            throw new KeycloakException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Extract user ID (sub) from JWT access token
     */
    private String extractUserIdFromToken(String accessToken) {
        try {
            log.debug("🔍 Extracting user ID from token...");

            // JWT format: header.payload.signature
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            // Decode payload (Base64 URL-safe)
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decodedBytes);

            log.debug("📄 JWT Payload: {}", payload);

            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(payload);

            // Extract 'sub' (subject) which is the user ID
            JsonNode subNode = jsonNode.get("sub");
            if (subNode == null) {
                throw new IllegalArgumentException("'sub' claim not found in JWT token");
            }

            String userId = subNode.asText();

            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("User ID (sub) is empty in token");
            }

            log.debug("✅ Extracted user ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("❌ Failed to extract user ID from token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract user ID from token", e);
        }
    }
}