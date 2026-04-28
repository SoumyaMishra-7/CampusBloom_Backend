package com.campusbloom.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SignupRequest(
        @NotBlank(message = "Role is required")
        String role,
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        String rollNumber,
        String department,
        String institutionName,
        String adminId,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}
