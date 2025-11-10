package com.collabrix.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Automatically configures Keycloak realm, clients, roles, and users on application startup.
 * This ensures that Keycloak is properly set up before the application starts accepting requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakInitializerService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @PostConstruct
    public void initializeKeycloak() {
        log.info("🚀 Starting Keycloak initialization...");

        try {
            // Step 1: Create or verify realm
            createRealmIfNotExists();

            // Step 2: Create clients
            createClientsIfNotExists();

            // Step 3: Create default roles
            createDefaultRoles();

            // Step 4: Create admin user
            createAdminUserIfNotExists();

            log.info("✅ Keycloak initialization completed successfully!");

        } catch (Exception e) {
            log.error("❌ Failed to initialize Keycloak: {}", e.getMessage(), e);
            // Don't throw exception - let application start even if Keycloak init fails
            // This allows manual configuration if needed
        }
    }

    /**
     * Create realm if it doesn't exist
     */
    private void createRealmIfNotExists() {
        try {
            RealmsResource realms = keycloakAdmin.realms();

            // Check if realm exists
            try {
                RealmResource existingRealm = realms.realm(realmName);
                existingRealm.toRepresentation(); // This will throw exception if not found
                log.info("✅ Realm '{}' already exists", realmName);
                return;
            } catch (NotFoundException e) {
                log.info("📝 Creating realm '{}'...", realmName);
            }

            // Create realm
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(realmName);
            realm.setEnabled(true);
            realm.setDisplayName("Collabrix Platform");
            realm.setDisplayNameHtml("<b>Collabrix</b> Platform");

            // Security settings
            realm.setSslRequired("none"); // Change to "external" in production
            realm.setRegistrationAllowed(false); // Users register via API
            realm.setRegistrationEmailAsUsername(false);
            realm.setRememberMe(true);
            realm.setVerifyEmail(true);
            realm.setLoginWithEmailAllowed(true);
            realm.setDuplicateEmailsAllowed(false);
            realm.setResetPasswordAllowed(true);
            realm.setEditUsernameAllowed(false);

            // Brute force protection
            realm.setBruteForceProtected(true);
            realm.setPermanentLockout(false);
            realm.setMaxFailureWaitSeconds(900);
            realm.setMinimumQuickLoginWaitSeconds(60);
            realm.setWaitIncrementSeconds(60);
            realm.setQuickLoginCheckMilliSeconds(1000L);
            realm.setMaxDeltaTimeSeconds(43200);
            realm.setFailureFactor(5);

            // Token settings
            realm.setAccessTokenLifespan(3600); // 1 hour
            realm.setAccessTokenLifespanForImplicitFlow(900); // 15 minutes
            realm.setSsoSessionIdleTimeout(1800); // 30 minutes
            realm.setSsoSessionMaxLifespan(36000); // 10 hours
            realm.setAccessCodeLifespan(60); // 1 minute
            realm.setAccessCodeLifespanUserAction(300); // 5 minutes
            realm.setAccessCodeLifespanLogin(1800); // 30 minutes

            // Events
            realm.setEventsEnabled(true);
            realm.setEventsListeners(Arrays.asList("jboss-logging"));
            realm.setEnabledEventTypes(Arrays.asList(
                    "LOGIN", "LOGIN_ERROR", "REGISTER", "REGISTER_ERROR",
                    "LOGOUT", "LOGOUT_ERROR", "CODE_TO_TOKEN", "REFRESH_TOKEN",
                    "UPDATE_EMAIL", "UPDATE_PASSWORD", "VERIFY_EMAIL"
            ));
            realm.setAdminEventsEnabled(true);
            realm.setAdminEventsDetailsEnabled(true);

            realms.create(realm);
            log.info("✅ Realm '{}' created successfully", realmName);

        } catch (Exception e) {
            log.error("❌ Error creating realm: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create realm", e);
        }
    }

    /**
     * Create clients if they don't exist
     */
    private void createClientsIfNotExists() {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realmName);

            // 1. Backend client (confidential)
            createBackendClient(realmResource);

            // 2. Frontend client (public)
            createFrontendClient(realmResource);

        } catch (Exception e) {
            log.error("❌ Error creating clients: {}", e.getMessage(), e);
        }
    }

    /**
     * Create backend client (confidential client for microservices)
     */
    private void createBackendClient(RealmResource realmResource) {
        try {
            // Check if client exists
            if (clientExists(realmResource, clientId)) {
                log.info("✅ Client '{}' already exists", clientId);
                return;
            }

            log.info("📝 Creating backend client '{}'...", clientId);

            ClientRepresentation client = new ClientRepresentation();
            client.setClientId(clientId);
            client.setName("Collabrix Backend Service");
            client.setDescription("Backend microservices client");
            client.setEnabled(true);
            client.setClientAuthenticatorType("client-secret");
            client.setSecret(clientSecret);
            client.setPublicClient(false); // Confidential client
            client.setBearerOnly(false);
            client.setStandardFlowEnabled(true);
            client.setImplicitFlowEnabled(false);
            client.setDirectAccessGrantsEnabled(true); // Resource Owner Password Credentials Grant
            client.setServiceAccountsEnabled(true);
            client.setAuthorizationServicesEnabled(true);
            client.setFullScopeAllowed(true);

            // Redirect URIs
            client.setRedirectUris(Arrays.asList(
                    "http://localhost:8081/*",
                    "http://localhost:8082/*",
                    "http://localhost:8083/*",
                    "http://localhost:3000/*",
                    "http://localhost:4200/*"
            ));

            // Web origins (CORS)
            client.setWebOrigins(Arrays.asList(
                    "http://localhost:8081",
                    "http://localhost:8082",
                    "http://localhost:8083",
                    "http://localhost:3000",
                    "http://localhost:4200",
                    "*" // Allow all in development (remove in production)
            ));

            // Protocol mappers
            client.setProtocolMappers(createProtocolMappers());

            // Create client
            Response response = realmResource.clients().create(client);
            if (response.getStatus() == 201) {
                log.info("✅ Backend client '{}' created successfully", clientId);
            } else {
                log.error("❌ Failed to create backend client. Status: {}", response.getStatus());
            }
            response.close();

        } catch (Exception e) {
            log.error("❌ Error creating backend client: {}", e.getMessage(), e);
        }
    }

    /**
     * Create frontend client (public client for web/mobile apps)
     */
    private void createFrontendClient(RealmResource realmResource) {
        try {
            String frontendClientId = "collabrix-frontend";

            // Check if client exists
            if (clientExists(realmResource, frontendClientId)) {
                log.info("✅ Client '{}' already exists", frontendClientId);
                return;
            }

            log.info("📝 Creating frontend client '{}'...", frontendClientId);

            ClientRepresentation client = new ClientRepresentation();
            client.setClientId(frontendClientId);
            client.setName("Collabrix Frontend Application");
            client.setDescription("Frontend web/mobile application");
            client.setEnabled(true);
            client.setPublicClient(true); // Public client (no secret)
            client.setBearerOnly(false);
            client.setStandardFlowEnabled(true);
            client.setImplicitFlowEnabled(false);
            client.setDirectAccessGrantsEnabled(true);
            client.setFullScopeAllowed(true);

            // Redirect URIs
            client.setRedirectUris(Arrays.asList(
                    "http://localhost:3000/*",
                    "http://localhost:4200/*"
            ));

            // Web origins (CORS)
            client.setWebOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:4200",
                    "*" // Allow all in development
            ));

            // Create client
            Response response = realmResource.clients().create(client);
            if (response.getStatus() == 201) {
                log.info("✅ Frontend client '{}' created successfully", frontendClientId);
            } else {
                log.error("❌ Failed to create frontend client. Status: {}", response.getStatus());
            }
            response.close();

        } catch (Exception e) {
            log.error("❌ Error creating frontend client: {}", e.getMessage(), e);
        }
    }

    /**
     * Create protocol mappers for JWT token claims
     */
    private List<org.keycloak.representations.idm.ProtocolMapperRepresentation> createProtocolMappers() {
        List<org.keycloak.representations.idm.ProtocolMapperRepresentation> mappers = new ArrayList<>();

        // 1. Realm roles mapper
        org.keycloak.representations.idm.ProtocolMapperRepresentation realmRolesMapper =
                new org.keycloak.representations.idm.ProtocolMapperRepresentation();
        realmRolesMapper.setName("realm-roles");
        realmRolesMapper.setProtocol("openid-connect");
        realmRolesMapper.setProtocolMapper("oidc-usermodel-realm-role-mapper");

        Map<String, String> realmRolesConfig = new HashMap<>();
        realmRolesConfig.put("multivalued", "true");
        realmRolesConfig.put("userinfo.token.claim", "true");
        realmRolesConfig.put("id.token.claim", "true");
        realmRolesConfig.put("access.token.claim", "true");
        realmRolesConfig.put("claim.name", "realm_access.roles");
        realmRolesConfig.put("jsonType.label", "String");
        realmRolesMapper.setConfig(realmRolesConfig);
        mappers.add(realmRolesMapper);

        // 2. Username mapper
        org.keycloak.representations.idm.ProtocolMapperRepresentation usernameMapper =
                new org.keycloak.representations.idm.ProtocolMapperRepresentation();
        usernameMapper.setName("username");
        usernameMapper.setProtocol("openid-connect");
        usernameMapper.setProtocolMapper("oidc-usermodel-property-mapper");

        Map<String, String> usernameConfig = new HashMap<>();
        usernameConfig.put("userinfo.token.claim", "true");
        usernameConfig.put("user.attribute", "username");
        usernameConfig.put("id.token.claim", "true");
        usernameConfig.put("access.token.claim", "true");
        usernameConfig.put("claim.name", "preferred_username");
        usernameConfig.put("jsonType.label", "String");
        usernameMapper.setConfig(usernameConfig);
        mappers.add(usernameMapper);

        // 3. Email mapper
        org.keycloak.representations.idm.ProtocolMapperRepresentation emailMapper =
                new org.keycloak.representations.idm.ProtocolMapperRepresentation();
        emailMapper.setName("email");
        emailMapper.setProtocol("openid-connect");
        emailMapper.setProtocolMapper("oidc-usermodel-property-mapper");

        Map<String, String> emailConfig = new HashMap<>();
        emailConfig.put("userinfo.token.claim", "true");
        emailConfig.put("user.attribute", "email");
        emailConfig.put("id.token.claim", "true");
        emailConfig.put("access.token.claim", "true");
        emailConfig.put("claim.name", "email");
        emailConfig.put("jsonType.label", "String");
        emailMapper.setConfig(emailConfig);
        mappers.add(emailMapper);

        return mappers;
    }

    /**
     * Create default roles
     */
    private void createDefaultRoles() {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realmName);

            List<String> defaultRoles = Arrays.asList(
                    "ROLE_ADMIN",
                    "ROLE_MANAGER",
                    "ROLE_USER",
                    "ROLE_GUEST"
            );

            for (String roleName : defaultRoles) {
                try {
                    // Check if role exists
                    realmResource.roles().get(roleName).toRepresentation();
                    log.info("✅ Role '{}' already exists", roleName);
                } catch (NotFoundException e) {
                    // Create role
                    RoleRepresentation role = new RoleRepresentation();
                    role.setName(roleName);
                    role.setDescription("Default " + roleName);
                    realmResource.roles().create(role);
                    log.info("✅ Role '{}' created successfully", roleName);
                }
            }

        } catch (Exception e) {
            log.error("❌ Error creating default roles: {}", e.getMessage(), e);
        }
    }

    /**
     * Create admin user if not exists
     */
    private void createAdminUserIfNotExists() {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realmName);

            // Check if admin user exists
            List<UserRepresentation> existingUsers = realmResource.users()
                    .search("admin", true);

            if (!existingUsers.isEmpty()) {
                log.info("✅ Admin user already exists");
                return;
            }

            log.info("📝 Creating admin user...");

            // Create admin user
            UserRepresentation adminUser = new UserRepresentation();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@collabrix.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEnabled(true);
            adminUser.setEmailVerified(true);

            // Create user
            Response response = realmResource.users().create(adminUser);
            if (response.getStatus() == 201) {
                String userId = getCreatedId(response);
                log.info("✅ Admin user created with ID: {}", userId);

                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue("admin123"); // Change in production!
                credential.setTemporary(false);
                realmResource.users().get(userId).resetPassword(credential);
                log.info("✅ Admin password set");

                // Assign ROLE_ADMIN and ROLE_USER
                RoleRepresentation adminRole = realmResource.roles().get("ROLE_ADMIN").toRepresentation();
                RoleRepresentation userRole = realmResource.roles().get("ROLE_USER").toRepresentation();
                realmResource.users().get(userId).roles().realmLevel()
                        .add(Arrays.asList(adminRole, userRole));
                log.info("✅ ROLE_ADMIN and ROLE_USER assigned to admin user");

            } else {
                log.error("❌ Failed to create admin user. Status: {}", response.getStatus());
            }
            response.close();

        } catch (Exception e) {
            log.error("❌ Error creating admin user: {}", e.getMessage(), e);
        }
    }

    // Helper Methods

    private boolean clientExists(RealmResource realmResource, String clientId) {
        return realmResource.clients().findByClientId(clientId).size() > 0;
    }

    private String getCreatedId(Response response) {
        String location = response.getHeaderString("Location");
        if (location != null) {
            return location.substring(location.lastIndexOf('/') + 1);
        }
        return null;
    }
}