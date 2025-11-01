package com.collabrix.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(nullable = false, length = 4)
    private String countryCode;

    @Column(nullable = false, length = 10)
    private String contactNo;

    @Column(length = 30)
    private String organization;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() { return active; }
    @Override
    public boolean isAccountNonLocked() { return active; }
    @Override
    public boolean isCredentialsNonExpired() { return active; }
    @Override
    public boolean isEnabled() { return active; }
}
