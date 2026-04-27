package com.campusbloom.backend.model;

public record CertificateStatusUpdateRequest(
        String status,
        String remarks
) {
}
