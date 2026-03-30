package com.collabrix.auth.service.interfaces;

public interface EmailVerificationService {
    void sendVerificationEmail(String userId);
    void verifyEmail(String userId);
    void resendVerificationEmail(String userId);
    boolean isEmailVerified(String userId);
}
