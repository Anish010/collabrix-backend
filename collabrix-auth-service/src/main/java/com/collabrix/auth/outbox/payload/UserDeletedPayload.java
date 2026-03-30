package com.collabrix.auth.outbox.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDeletedPayload {
    private String userId;
    private String username;
}