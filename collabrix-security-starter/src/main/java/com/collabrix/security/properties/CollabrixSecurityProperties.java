package com.collabrix.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "collabrix.security")
public class CollabrixSecurityProperties {
    /**
     * Enable/disable security auto-configuration
     */
    private boolean enabled = true;

    /**
     * Keycloak issuer URI (realm URL)
     * Example: <a href="http://localhost:8080/realms/collabrix">...</a>
     */
    private String issuerUri;

    /**
     * JWT claim path for roles
     * Default: "roles" but Keycloak uses "realm_access.roles"
     */
    private String rolesClaim = "roles";
}