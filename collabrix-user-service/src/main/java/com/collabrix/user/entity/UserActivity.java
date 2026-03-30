package com.collabrix.user.entity;

import com.collabrix.user.dto.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_activity",
        indexes = @Index(name = "idx_user_activity_user", columnList = "keycloakUserId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String keycloakUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
