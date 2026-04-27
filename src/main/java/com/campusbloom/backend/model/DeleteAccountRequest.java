package com.campusbloom.backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "Role is required")
        String role,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
