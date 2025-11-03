package com.collabrix.security.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts Keycloak JWT claims to Spring Security authorities.
 * Supports:
 * - realm_access.roles (Keycloak realm roles)
 * - resource_access.{client}.roles (Keycloak client roles)
 * - Custom roles claim
 */
public class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String rolesClaim;

    public KeycloakGrantedAuthoritiesConverter(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        // 1. Custom roles claim (if configured)
        if (rolesClaim != null && jwt.hasClaim(rolesClaim)) {
            Object raw = jwt.getClaim(rolesClaim);
            if (raw instanceof Collection<?> collection) {
                collection.forEach(o -> roles.add(String.valueOf(o)));
            } else if (raw != null) {
                roles.add(String.valueOf(raw));
            }
        }

        // 2. Keycloak realm roles: realm_access.roles
        if (jwt.hasClaim("realm_access")) {
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?, ?> realmMap) {
                Object rolesObj = realmMap.get("roles");
                if (rolesObj instanceof Collection<?> rolesList) {
                    rolesList.forEach(o -> roles.add(String.valueOf(o)));
                }
            }
        }

        // 3. Keycloak client roles: resource_access.{client}.roles
        if (jwt.hasClaim("resource_access")) {
            Object resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess instanceof Map<?, ?> resourceMap) {
                for (Object clientObj : resourceMap.values()) {
                    if (clientObj instanceof Map<?, ?> clientMap) {
                        Object rolesObj = clientMap.get("roles");
                        if (rolesObj instanceof Collection<?> rolesList) {
                            rolesList.forEach(o -> roles.add(String.valueOf(o)));
                        }
                    }
                }
            }
        }

        // Add ROLE_ prefix if not present (Spring Security convention)
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : ("ROLE_" + r))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}