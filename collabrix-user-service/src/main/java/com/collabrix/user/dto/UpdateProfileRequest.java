package com.collabrix.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 3)
    private String countryCode;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String contactNo;

    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String organization;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Pattern(regexp = "^(https?://)?(www\\.)?linkedin\\.com/.*$", message = "Invalid LinkedIn URL")
    private String linkedinUrl;

    @Pattern(regexp = "^(https?://)?(www\\.)?github\\.com/.*$", message = "Invalid GitHub URL")
    private String githubUrl;

    @Pattern(regexp = "^(https?://)?(www\\.)?(twitter|x)\\.com/.*$", message = "Invalid Twitter URL")
    private String twitterUrl;

    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$",
            message = "Invalid website URL")
    private String websiteUrl;
}