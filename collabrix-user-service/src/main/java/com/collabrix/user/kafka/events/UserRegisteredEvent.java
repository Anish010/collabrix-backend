package com.collabrix.user.kafka.events;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent {
    private String eventId;
    private String eventType;
    private Long timestamp;
    private String keycloakUserId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String contactNo;
    private String organization;
    private List<String> roles;
}