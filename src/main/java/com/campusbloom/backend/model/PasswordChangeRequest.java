package com.campusbloom.backend.model;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}
