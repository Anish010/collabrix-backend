package com.collabrix.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for register endpoint.
 */
@Data
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String firstName;

    private String lastName;


    @NotBlank
    private String countryCode;

    @NotBlank
    private String contactNo;

    private String organization;
}
