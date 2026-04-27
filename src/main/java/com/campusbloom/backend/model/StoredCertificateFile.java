package com.campusbloom.backend.model;

import org.springframework.core.io.Resource;

public record StoredCertificateFile(
        Resource resource,
        String contentType,
        String fileName
) {
}
