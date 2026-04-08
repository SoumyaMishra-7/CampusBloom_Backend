package com.campusbloom.backend.model;

import java.time.Instant;

public record CaptchaChallengeResponse(
        String captchaId,
        String prompt,
        Instant expiresAt
) {
}
