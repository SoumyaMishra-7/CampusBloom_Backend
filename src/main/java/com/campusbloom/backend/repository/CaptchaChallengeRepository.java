package com.campusbloom.backend.repository;

import com.campusbloom.backend.model.CaptchaChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaptchaChallengeRepository extends JpaRepository<CaptchaChallenge, String> {
}
