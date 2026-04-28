package com.campusbloom.backend.controller;

import com.campusbloom.backend.model.ActionResponse;
import com.campusbloom.backend.model.AdminRegistrationRequest;
import com.campusbloom.backend.model.AuthLoginRequest;
import com.campusbloom.backend.model.AuthResponse;
import com.campusbloom.backend.model.DashboardResponse;
import com.campusbloom.backend.model.SignupRequest;
import com.campusbloom.backend.model.StudentRegistrationRequest;
import com.campusbloom.backend.service.AuthService;
import com.campusbloom.backend.service.StudentPortalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiCompatibilityController {

    private final AuthService authService;
    private final StudentPortalService studentPortalService;

    public ApiCompatibilityController(AuthService authService, StudentPortalService studentPortalService) {
        this.authService = authService;
        this.studentPortalService = studentPortalService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/signup")
    public ActionResponse signup(@Valid @RequestBody SignupRequest request) {
        return switch (normalizeRole(request.role())) {
            case "student" -> authService.registerStudent(new StudentRegistrationRequest(
                    request.fullName(),
                    request.email(),
                    requiredValue(request.rollNumber(), "Roll number is required for student signup"),
                    requiredValue(request.department(), "Department is required for student signup"),
                    request.password(),
                    request.confirmPassword()
            ));
            case "admin" -> authService.registerAdmin(new AdminRegistrationRequest(
                    request.fullName(),
                    request.email(),
                    requiredValue(request.institutionName(), "Institution name is required for admin signup"),
                    requiredValue(request.adminId(), "Admin ID is required for admin signup"),
                    request.password(),
                    request.confirmPassword()
            ));
            default -> throw new IllegalArgumentException("Role must be either student or admin");
        };
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(defaultValue = "student") String role) {
        String normalizedRole = normalizeRole(role);
        if (!"student".equals(normalizedRole)) {
            throw new IllegalArgumentException("Dashboard endpoint currently supports student role only");
        }
        return studentPortalService.getDashboard();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        return role.trim().toLowerCase();
    }

    private String requiredValue(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
