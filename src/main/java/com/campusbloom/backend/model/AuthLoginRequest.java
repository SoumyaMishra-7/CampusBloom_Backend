package com.campusbloom.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthLoginRequest(
        @NotBlank(message = "Role is required")
        String role,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Captcha challenge is required")
        String captchaId,
        @NotBlank(message = "Captcha answer is required")
        String captchaAnswer,
        @NotBlank(message = "Password is required")
        String password
) {
}
