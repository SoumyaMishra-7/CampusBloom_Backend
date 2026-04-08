package com.campusbloom.backend.model;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "Role is required")
        String role,
        String email,
        String identifier,
        @NotBlank(message = "Captcha challenge is required")
        String captchaId,
        @NotBlank(message = "Captcha answer is required")
        String captchaAnswer,
        @NotBlank(message = "Password is required")
        String password
) {
}
