package com.campusbloom.backend.model;

public record DashboardMetric(
        String key,
        String label,
        int value,
        String icon
) {
}
