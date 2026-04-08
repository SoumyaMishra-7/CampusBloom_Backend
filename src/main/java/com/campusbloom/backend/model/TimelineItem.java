package com.campusbloom.backend.model;

public record TimelineItem(
        String date,
        String title,
        String note,
        String category
) {
}
