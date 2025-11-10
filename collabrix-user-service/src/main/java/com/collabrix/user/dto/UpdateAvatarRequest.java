package com.collabrix.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating avatar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarRequest {

    @NotBlank(message = "Avatar URL is required")
    @Pattern(regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$",
            message = "Invalid image URL format")
    private String avatarUrl;
}