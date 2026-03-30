package com.collabrix.auth.service.interfaces;

import com.collabrix.auth.dto.UserRegisterRequest;
import com.collabrix.auth.dto.UserResponse;

public interface RegistrationOrchestratorService {
    UserResponse registerUser(UserRegisterRequest request);
}
