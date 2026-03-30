package com.collabrix.auth.outbox.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoleChangedPayload {
    private String userId;
    private String roleName;
    private String action; // ASSIGNED / REMOVED
}