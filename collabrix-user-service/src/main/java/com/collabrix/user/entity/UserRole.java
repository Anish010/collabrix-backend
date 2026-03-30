package com.collabrix.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"keycloakUserId", "roleName"}),
        indexes = @Index(name="idx_user_role", columnList="keycloakUserId,roleName")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String keycloakUserId;

    @Column(nullable = false)
    private String roleName;
}
