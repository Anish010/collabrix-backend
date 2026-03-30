package com.collabrix.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for handling email verification.
 * This is a placeholder - full implementation will use Keycloak's built-in email verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Send verification email to user
     * Uses Keycloak's built-in email verification action
     */
    public void sendVerificationEmail(String userId) {
        log.info("📧 Sending verification email to user: {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            // Send verification email using Keycloak's action
            userResource.executeActionsEmail(Collections.singletonList("VERIFY_EMAIL"));

            log.info("✅ Verification email sent to user: {}", userId);

        } catch (Exception e) {
            log.error("❌ Failed to send verification email: {}", e.getMessage());
            // Don't throw exception - email sending failure shouldn't block registration
        }
    }

    /**
     * Verify email manually (if needed)
     */
    public void verifyEmail(String userId) {
        log.info("✅ Manually verifying email for user: {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            user.setEmailVerified(true);
            userResource.update(user);

            log.info("✅ Email verified for user: {}", userId);

        } catch (Exception e) {
            log.error("❌ Failed to verify email: {}", e.getMessage());
            throw new RuntimeException("Failed to verify email: " + e.getMessage());
        }
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String userId) {
        log.info("🔄 Resending verification email to user: {}", userId);
        sendVerificationEmail(userId);
    }

    /**
     * Check if email is verified
     */
    public boolean isEmailVerified(String userId) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            return user.isEmailVerified();

        } catch (Exception e) {
            log.error("❌ Failed to check email verification status: {}", e.getMessage());
            return false;
        }
    }
}