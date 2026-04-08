package com.campusbloom.backend.model;

import java.util.List;

public record Achievement(
        Long id,
        String title,
        String category,
        String level,
        String date,
        String status,
        List<String> skills,
        String description,
        boolean featured,
        boolean favorite
) {
}
