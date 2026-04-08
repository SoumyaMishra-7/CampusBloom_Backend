package com.campusbloom.backend.model;

public record NotificationItem(
        Long id,
        String icon,
        String title,
        String description
) {
}
