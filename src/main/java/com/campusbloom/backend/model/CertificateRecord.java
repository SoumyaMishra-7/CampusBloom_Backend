package com.campusbloom.backend.model;

public record CertificateRecord(
        Long id,
        String title,
        String event,
        String uploadedAt,
        String category,
        String type,
        double sizeMB,
        String status,
        String achievementLink,
        String remarks,
        String previewKind
) {
}
