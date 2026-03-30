package com.collabrix.auth.outbox.payload;

import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.dto.UserRegisterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisteredPayload {
    private UserResponse authUser;
    private UserRegisterRequest request;
}
