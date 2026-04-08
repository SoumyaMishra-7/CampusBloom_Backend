package com.campusbloom.backend.model;

public record StudentPrivacySettings(
        boolean publicProfile,
        boolean showEmail,
        boolean showRollNumber,
        boolean achievementVisibility
) {
}
