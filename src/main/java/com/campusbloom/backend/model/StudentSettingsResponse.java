package com.campusbloom.backend.model;

public record StudentSettingsResponse(
        StudentProfileSettings profile,
        StudentPrivacySettings privacy,
        StudentNotificationSettings notifications,
        StudentAppearanceSettings appearance
) {
}
