package com.campusbloom.backend.controller;

import com.campusbloom.backend.model.ActionResponse;
import com.campusbloom.backend.model.AdminRegistrationRequest;
import com.campusbloom.backend.model.AuthLoginRequest;
import com.campusbloom.backend.model.AuthResponse;
import com.campusbloom.backend.model.CaptchaChallengeResponse;
import com.campusbloom.backend.model.StudentRegistrationRequest;
import com.campusbloom.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/captcha")
    public CaptchaChallengeResponse getCaptcha() {
        return authService.createCaptchaChallenge();
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register/student")
    public ActionResponse registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        return authService.registerStudent(request);
    }

    @PostMapping("/register/admin")
    public ActionResponse registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        return authService.registerAdmin(request);
    }
}
