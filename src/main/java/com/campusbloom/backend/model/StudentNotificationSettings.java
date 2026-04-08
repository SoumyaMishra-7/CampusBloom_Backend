package com.campusbloom.backend.model;

public record StudentNotificationSettings(
        boolean achievementAdded,
        boolean approvalUpdate,
        boolean weeklySummary
) {
}
