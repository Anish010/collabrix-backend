package com.collabrix.auth.service;

import com.collabrix.auth.entity.Role;
import com.collabrix.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps our User into Spring Security's UserDetails.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final UUIDConverter id;
    private final String username;
    private final String password;
    private final Set<Role> roles;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.id = new UUIDConverter(user.getId());
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roles = user.getRoles();
        this.active = user.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return active; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return active; }

    @Override
    public boolean isEnabled() { return active; }

    /**
     * Small helper to get string UUID (avoid exposing UUID directly everywhere).
     */
    public String getIdAsString() { return id.toString(); }

    // Simple wrapper class because lombok/getters can't be used on UUID when customizing
    public static class UUIDConverter {
        private final java.util.UUID uuid;
        public UUIDConverter(java.util.UUID uuid) { this.uuid = uuid; }
        @Override public String toString() { return uuid == null ? null : uuid.toString(); }
    }
}
