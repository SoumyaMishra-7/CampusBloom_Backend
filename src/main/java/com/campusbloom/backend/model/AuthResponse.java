package com.campusbloom.backend.model;

public record AuthResponse(
        String role,
        String redirectTo,
        String message
) {
}
