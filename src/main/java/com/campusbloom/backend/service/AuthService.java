package com.campusbloom.backend.service;

import com.campusbloom.backend.model.AppUser;
import com.campusbloom.backend.model.AppUserRole;
import com.campusbloom.backend.model.ActionResponse;
import com.campusbloom.backend.model.AdminRegistrationRequest;
import com.campusbloom.backend.model.AuthLoginRequest;
import com.campusbloom.backend.model.AuthResponse;
import com.campusbloom.backend.model.CaptchaChallenge;
import com.campusbloom.backend.model.CaptchaChallengeResponse;
import com.campusbloom.backend.model.DeleteAccountRequest;
import com.campusbloom.backend.model.StudentRegistrationRequest;
import com.campusbloom.backend.repository.AppUserRepository;
import com.campusbloom.backend.repository.CaptchaChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final CaptchaChallengeRepository captchaChallengeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository appUserRepository,
            CaptchaChallengeRepository captchaChallengeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.captchaChallengeRepository = captchaChallengeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CaptchaChallengeResponse createCaptchaChallenge() {
        int left = ThreadLocalRandom.current().nextInt(10, 50);
        int right = ThreadLocalRandom.current().nextInt(1, 10);
        boolean useAddition = ThreadLocalRandom.current().nextBoolean();

        String prompt;
        int answer;

        if (useAddition) {
            prompt = "What is " + left + " + " + right + "?";
            answer = left + right;
        } else {
            prompt = "What is " + left + " - " + right + "?";
            answer = left - right;
        }

        CaptchaChallenge challenge = new CaptchaChallenge();
        challenge.setId(UUID.randomUUID().toString());
        challenge.setPrompt(prompt);
        challenge.setAnswerHash(passwordEncoder.encode(Integer.toString(answer)));
        challenge.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
        captchaChallengeRepository.save(challenge);

        return new CaptchaChallengeResponse(challenge.getId(), challenge.getPrompt(), challenge.getExpiresAt());
    }

    @Transactional
    public AuthResponse login(AuthLoginRequest request) {
        validateCaptcha(request.captchaId(), request.captchaAnswer());
        AppUserRole role = parseRole(request.role());
        AppUser user = appUserRepository.findByRoleAndEmailIgnoreCase(role, normalize(request.email()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String redirectTo = role == AppUserRole.ADMIN ? "/admin-dashboard" : "/student-dashboard";
        String message = role == AppUserRole.ADMIN ? "Admin login successful" : "Student login successful";
        return new AuthResponse(role.name().toLowerCase(), redirectTo, message);
    }

    @Transactional
    public ActionResponse registerStudent(StudentRegistrationRequest request) {
        validatePasswords(request.password(), request.confirmPassword());
        ensureEmailAvailable(request.email());
        ensureRollNumberAvailable(request.rollNumber());

        AppUser user = new AppUser();
        user.setRole(AppUserRole.STUDENT);
        user.setFullName(request.fullName().trim());
        user.setEmail(normalize(request.email()));
        user.setRollNumber(normalize(request.rollNumber()));
        user.setDepartment(request.department().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        appUserRepository.save(user);

        return new ActionResponse("Student account created successfully");
    }

    @Transactional
    public ActionResponse registerAdmin(AdminRegistrationRequest request) {
        validatePasswords(request.password(), request.confirmPassword());
        ensureEmailAvailable(request.email());
        ensureAdminIdAvailable(request.adminId());

        AppUser user = new AppUser();
        user.setRole(AppUserRole.ADMIN);
        user.setFullName(request.fullName().trim());
        user.setEmail(normalize(request.email()));
        user.setInstitutionName(request.institutionName().trim());
        user.setAdminId(normalize(request.adminId()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        appUserRepository.save(user);

        return new ActionResponse("Admin account created successfully");
    }

    @Transactional
    public ActionResponse deleteAccount(DeleteAccountRequest request) {
        AppUserRole role = parseRole(request.role());
        AppUser user = appUserRepository.findByRoleAndEmailIgnoreCase(role, normalize(request.email()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        appUserRepository.delete(user);
        String accountType = role == AppUserRole.ADMIN ? "Admin" : "Student";
        return new ActionResponse(accountType + " account deleted successfully");
    }

    private void validatePasswords(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
    }

    private void validateCaptcha(String captchaId, String captchaAnswer) {
        CaptchaChallenge challenge = captchaChallengeRepository.findById(captchaId)
                .orElseThrow(() -> new IllegalArgumentException("Captcha has expired. Refresh and try again"));

        if (challenge.getUsedAt() != null) {
            throw new IllegalArgumentException("Captcha has already been used. Refresh and try again");
        }

        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Captcha has expired. Refresh and try again");
        }

        String normalizedAnswer = captchaAnswer == null ? "" : captchaAnswer.trim();
        if (!passwordEncoder.matches(normalizedAnswer, challenge.getAnswerHash())) {
            throw new IllegalArgumentException("Incorrect captcha answer");
        }

        challenge.setUsedAt(Instant.now());
        captchaChallengeRepository.save(challenge);
    }

    private AppUserRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        return switch (rawRole.trim().toLowerCase()) {
            case "student" -> AppUserRole.STUDENT;
            case "admin" -> AppUserRole.ADMIN;
            default -> throw new IllegalArgumentException("Role must be either student or admin");
        };
    }

    private void ensureEmailAvailable(String email) {
        if (appUserRepository.existsByEmailIgnoreCase(normalize(email))) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
    }

    private void ensureRollNumberAvailable(String rollNumber) {
        if (appUserRepository.existsByRoleAndRollNumberIgnoreCase(AppUserRole.STUDENT, normalize(rollNumber))) {
            throw new IllegalArgumentException("An account with this roll number already exists");
        }
    }

    private void ensureAdminIdAvailable(String adminId) {
        if (appUserRepository.existsByRoleAndAdminIdIgnoreCase(AppUserRole.ADMIN, normalize(adminId))) {
            throw new IllegalArgumentException("An account with this admin ID already exists");
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
