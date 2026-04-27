package com.campusbloom.backend.controller;

import com.campusbloom.backend.model.*;
import com.campusbloom.backend.service.StudentPortalService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    public StudentPortalController(StudentPortalService studentPortalService) {
        this.studentPortalService = studentPortalService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return studentPortalService.getDashboard();
    }

    @GetMapping("/achievements")
    public List<Achievement> getAchievements() {
        return studentPortalService.getAchievements();
    }

    @GetMapping("/timeline")
    public List<TimelineItem> getTimeline() {
        return studentPortalService.getTimeline();
    }

    @GetMapping("/notifications")
    public List<NotificationItem> getNotifications() {
        return studentPortalService.getNotifications();
    }

    @GetMapping("/certificates")
    public List<CertificateRecord> getCertificates() {
        return studentPortalService.getCertificates();
    }

    @PostMapping(value = "/certificates/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CertificateRecord uploadCertificate(
            @RequestParam String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam MultipartFile file
    ) {
        return studentPortalService.uploadCertificate(title, category, description, file);
    }

    @PatchMapping("/certificates/{certificateId}/status")
    public CertificateRecord updateCertificateStatus(
            @PathVariable Long certificateId,
            @RequestBody CertificateStatusUpdateRequest request
    ) {
        return studentPortalService.updateCertificateStatus(certificateId, request.status(), request.remarks());
    }

    @GetMapping("/certificates/{certificateId}/file")
    public ResponseEntity<Resource> getCertificateFile(@PathVariable Long certificateId) {
        StoredCertificateFile storedFile = studentPortalService.loadCertificateFile(certificateId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storedFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + storedFile.fileName() + "\"")
                .body(storedFile.resource());
    }

    @GetMapping("/profile/public")
    public PublicProfileResponse getPublicProfile() {
        return studentPortalService.getPublicProfile();
    }

    @PostMapping("/profile/export")
    public ActionResponse exportPortfolio() {
        return studentPortalService.exportPortfolio();
    }

    @PostMapping("/profile/share")
    public ActionResponse sharePublicProfile() {
        return studentPortalService.sharePublicProfile();
    }

    @GetMapping("/settings")
    public StudentSettingsResponse getSettings() {
        return studentPortalService.getSettings();
    }

    @PutMapping("/settings/profile")
    public StudentProfileSettings updateProfile(@RequestBody StudentProfileSettings profile) {
        return studentPortalService.updateProfile(profile);
    }

    @PutMapping("/settings/privacy")
    public StudentPrivacySettings updatePrivacy(@RequestBody StudentPrivacySettings privacy) {
        return studentPortalService.updatePrivacy(privacy);
    }

    @PutMapping("/settings/notifications")
    public StudentNotificationSettings updateNotifications(@RequestBody StudentNotificationSettings notifications) {
        return studentPortalService.updateNotifications(notifications);
    }

    @PutMapping("/settings/appearance")
    public StudentAppearanceSettings updateAppearance(@RequestBody StudentAppearanceSettings appearance) {
        return studentPortalService.updateAppearance(appearance);
    }

    @PostMapping("/account/change-password")
    public ActionResponse changePassword(@RequestBody PasswordChangeRequest request) {
        return studentPortalService.changePassword(request);
    }

    @PostMapping("/account/delete-request")
    public ActionResponse submitDeleteRequest() {
        return studentPortalService.submitDeleteRequest();
    }
}
