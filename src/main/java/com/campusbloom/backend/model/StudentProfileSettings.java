package com.campusbloom.backend.model;

public record StudentProfileSettings(
        String fullName,
        String email,
        String department,
        String year,
        String bio,
        String photoName
) {
}
