package com.campusbloom.backend.service;

import com.campusbloom.backend.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StudentPortalService {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final AtomicLong certificateIdSequence = new AtomicLong(100);
    private final Path uploadDirectory;

    private StudentProfileSettings profile = new StudentProfileSettings(
            "Soumya Mishra",
            "soumya@example.edu",
            "Computer Science",
            "Final Year",
            "Student builder focused on technology, leadership, and evidence-backed extracurricular growth.",
            ""
    );
    private StudentPrivacySettings privacy = new StudentPrivacySettings(true, false, false, true);
    private StudentNotificationSettings notificationSettings = new StudentNotificationSettings(true, true, false);
    private StudentAppearanceSettings appearance = new StudentAppearanceSettings("system", false);

    private final List<Achievement> achievements = new ArrayList<>(List.of(
            new Achievement(1L, "Smart Campus IoT Hackathon Winner", "Technical", "National", "2026-02-12", "Approved",
                    List.of("IoT", "Python", "Team Leadership"),
                    "Led a team to build an IoT-driven campus energy optimization prototype with real-time sensor monitoring.", true, true),
            new Achievement(2L, "Intercollege Basketball Tournament", "Sports", "College", "2026-01-28", "Approved",
                    List.of("Teamwork", "Discipline", "Strategy"),
                    "Represented department team and contributed in semifinals and finals with strategic coordination.", true, false),
            new Achievement(3L, "State Cultural Fusion Performance", "Cultural", "State", "2026-01-15", "Pending",
                    List.of("Stage Performance", "Coordination", "Creativity"),
                    "Performed in a multidisciplinary cultural showcase combining classical and contemporary formats.", true, false),
            new Achievement(4L, "Student Council Event Lead", "Leadership", "College", "2025-12-20", "Approved",
                    List.of("Event Planning", "Communication", "Execution"),
                    "Managed inter-department event planning, volunteer coordination, and execution logistics.", false, false),
            new Achievement(5L, "Open Source Contribution Sprint", "Technical", "State", "2025-12-05", "Approved",
                    List.of("Git", "React", "Problem Solving"),
                    "Contributed UI improvements and bug fixes to a student-led open-source initiative.", false, true),
            new Achievement(6L, "Track & Field 400m Finals", "Sports", "State", "2025-11-19", "Pending",
                    List.of("Athletics", "Consistency", "Time Management"),
                    "Qualified for the state finals and maintained consistent performance across heats.", false, false)
    ));

    private final List<TimelineItem> timelineItems = List.of(
            new TimelineItem("Feb 2026", "National Hackathon Winner", "Approved by Innovation Cell", "Technical"),
            new TimelineItem("Jan 2026", "Intercollege Basketball Tournament", "Certificate verified and portfolio published", "Sports"),
            new TimelineItem("Jan 2026", "State Cultural Performance Submission", "Awaiting faculty coordinator approval", "Cultural"),
            new TimelineItem("Dec 2025", "Student Council Event Leadership", "Added impact metrics and event photos", "Leadership")
    );

    private final List<NotificationItem> notifications = List.of(
            new NotificationItem(1L, "check", "Hackathon achievement approved", "Innovation Cell verified your national-level entry."),
            new NotificationItem(2L, "upload", "Certificate upload reminder", "Add proof for your cultural performance to complete review."),
            new NotificationItem(3L, "share", "Profile viewed by placement cell", "Your public profile was accessed today.")
    );

    private final List<CertificateRecord> certificates = new ArrayList<>();

    private final List<Badge> badges = List.of(
            new Badge("Gold - Innovation", "gold"),
            new Badge("Silver - Leadership", "silver"),
            new Badge("Bronze - Consistency", "bronze")
    );

    public StudentPortalService(@Value("${app.certificates.upload-dir:uploads/certificates}") String uploadDir) {
        try {
            this.uploadDirectory = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize certificate upload storage", exception);
        }

        certificates.addAll(List.of(
                seedCertificate(1L, "Smart Campus IoT Hackathon Winner", "National Innovation Hackathon", "2026-02-15",
                        "Technical", "PDF", 1.8, "Verified",
                        "Verified by Innovation Cell and faculty mentor.", "pdf"),
                seedCertificate(2L, "Intercollege Basketball Tournament", "Sports Council Meet", "2026-01-29",
                        "Sports", "Image", 2.4, "Verified",
                        "Uploaded scorecard and participation certificate validated.", "image"),
                seedCertificate(3L, "State Cultural Fusion Performance", "Kala Utsav", "2026-01-17",
                        "Cultural", "PDF", 1.3, "Pending",
                        "Awaiting department coordinator verification remarks.", "pdf"),
                seedCertificate(4L, "Student Council Event Lead", "Annual Tech-Cultural Fest", "2025-12-22",
                        "Leadership", "Image", 3.1, "Verified",
                        "Committee approval verified with organizing team signatures.", "image")
        ));
    }

    public DashboardResponse getDashboard() {
        List<DashboardMetric> stats = List.of(
                new DashboardMetric("total", "Total Achievements", 148, "trophy"),
                new DashboardMetric("awards", "Awards Won", 32, "medal"),
                new DashboardMetric("certs", "Certificates Uploaded", 64, "certificate"),
                new DashboardMetric("events", "Participation Events", 96, "target")
        );

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("Technical", 2);
        categoryCounts.put("Sports", 2);
        categoryCounts.put("Cultural", 1);
        categoryCounts.put("Leadership", 1);

        List<QuickAction> quickActions = List.of(
                new QuickAction("Upload Certificate", "upload"),
                new QuickAction("Generate Portfolio PDF", "download"),
                new QuickAction("Share Public Profile", "share"),
                new QuickAction("Edit Profile", "edit")
        );

        List<String> spotlightSkills = List.of(
                "Leadership", "Problem Solving", "Teamwork", "Communication", "Execution",
                "Creativity", "Discipline", "React", "IoT", "Event Planning"
        );

        return new DashboardResponse(
                profile.fullName(),
                84,
                78,
                stats,
                categoryCounts,
                List.copyOf(achievements),
                List.copyOf(timelineItems),
                List.copyOf(notifications),
                spotlightSkills,
                quickActions
        );
    }

    public List<Achievement> getAchievements() {
        return List.copyOf(achievements);
    }

    public List<TimelineItem> getTimeline() {
        return List.copyOf(timelineItems);
    }

    public List<NotificationItem> getNotifications() {
        return List.copyOf(notifications);
    }

    public List<CertificateRecord> getCertificates() {
        return List.copyOf(certificates);
    }

    public CertificateRecord uploadCertificate(String title, String category, String description, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Certificate file is required");
        }

        long nextId = certificateIdSequence.incrementAndGet();
        String normalizedTitle = StringUtils.hasText(title) ? title.trim() : file.getOriginalFilename();
        String normalizedCategory = StringUtils.hasText(category) ? category.trim() : "General";
        String normalizedType = resolveFileType(file);
        String previewKind = normalizedType.equals("PDF") ? "pdf" : "image";
        String storedFileName = storeCertificateFile(nextId, file, normalizedType);
        String uploadedAt = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        CertificateRecord record = new CertificateRecord(
                nextId,
                normalizedTitle,
                "Manual Upload",
                uploadedAt,
                normalizedCategory,
                normalizedType,
                roundFileSize(file.getSize()),
                "Pending",
                "/student-achievements",
                StringUtils.hasText(description) ? description.trim() : "Awaiting admin review and verification remarks.",
                previewKind,
                buildFileUrl(nextId),
                storedFileName
        );
        certificates.add(0, record);
        return record;
    }

    public CertificateRecord updateCertificateStatus(Long certificateId, String status, String remarks) {
        String normalizedStatus = normalizeCertificateStatus(status);
        int certificateIndex = findCertificateIndex(certificateId);
        CertificateRecord existing = certificates.get(certificateIndex);

        String nextRemarks = normalizeModerationRemarks(normalizedStatus, remarks, existing.remarks());
        String moderationTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        CertificateRecord updated = new CertificateRecord(
                existing.id(),
                existing.title(),
                existing.event(),
                moderationTimestamp,
                existing.category(),
                existing.type(),
                existing.sizeMB(),
                normalizedStatus,
                existing.achievementLink(),
                nextRemarks,
                existing.previewKind(),
                existing.fileUrl(),
                existing.storedFileName()
        );

        certificates.set(certificateIndex, updated);
        return updated;
    }

    public StoredCertificateFile loadCertificateFile(Long certificateId) {
        CertificateRecord record = findCertificate(certificateId);
        if (!StringUtils.hasText(record.storedFileName())) {
            throw new IllegalArgumentException("Certificate file not found");
        }
        Path filePath = uploadDirectory.resolve(record.storedFileName()).normalize();
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Certificate file not found");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Certificate file not found");
            }
            String contentType = Files.probeContentType(filePath);
            return new StoredCertificateFile(
                    resource,
                    StringUtils.hasText(contentType) ? contentType : resolveContentType(record.type()),
                    filePath.getFileName().toString()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read certificate file", exception);
        }
    }

    public PublicProfileResponse getPublicProfile() {
        List<DashboardMetric> stats = List.of(
                new DashboardMetric("total", "Total Achievements", 148, "trophy"),
                new DashboardMetric("awards", "Awards Won", 32, "medal"),
                new DashboardMetric("events", "Participation Events", 96, "target"),
                new DashboardMetric("certs", "Certificates Uploaded", 64, "certificate")
        );

        return new PublicProfileResponse(
                profile.fullName(),
                profile.department(),
                profile.year(),
                profile.bio(),
                privacy.publicProfile(),
                84,
                stats,
                List.copyOf(achievements),
                List.copyOf(timelineItems),
                badges
        );
    }

    public StudentSettingsResponse getSettings() {
        return new StudentSettingsResponse(profile, privacy, notificationSettings, appearance);
    }

    public StudentProfileSettings updateProfile(StudentProfileSettings nextProfile) {
        profile = nextProfile;
        return profile;
    }

    public StudentPrivacySettings updatePrivacy(StudentPrivacySettings nextPrivacy) {
        privacy = nextPrivacy;
        return privacy;
    }

    public StudentNotificationSettings updateNotifications(StudentNotificationSettings nextNotifications) {
        notificationSettings = nextNotifications;
        return notificationSettings;
    }

    public StudentAppearanceSettings updateAppearance(StudentAppearanceSettings nextAppearance) {
        appearance = nextAppearance;
        return appearance;
    }

    public ActionResponse changePassword(PasswordChangeRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password must match");
        }
        return new ActionResponse("Password updated successfully");
    }

    public ActionResponse submitDeleteRequest() {
        return new ActionResponse("Delete request submitted for admin confirmation");
    }

    public ActionResponse exportPortfolio() {
        return new ActionResponse("Portfolio export generated successfully");
    }

    public ActionResponse sharePublicProfile() {
        return new ActionResponse("Public profile link generated successfully");
    }

    private CertificateRecord seedCertificate(
            Long id,
            String title,
            String event,
            String uploadedAt,
            String category,
            String type,
            double sizeMB,
            String status,
            String remarks,
            String previewKind
    ) {
        return new CertificateRecord(
                id,
                title,
                event,
                uploadedAt,
                category,
                type,
                sizeMB,
                status,
                "/student-achievements",
                remarks,
                previewKind,
                placeholderPreviewUrl(title, status),
                null
        );
    }

    private String placeholderPreviewUrl(String title, String status) {
        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='1200' height='1600' viewBox='0 0 1200 1600'>
                  <rect width='1200' height='1600' fill='#f8fafc'/>
                  <rect x='70' y='70' width='1060' height='1460' rx='40' fill='#ffffff' stroke='#cbd5e1' stroke-width='6'/>
                  <text x='120' y='210' font-family='Arial' font-size='44' fill='#0f172a'>CampusBloom Certificate Preview</text>
                  <text x='120' y='310' font-family='Arial' font-size='68' font-weight='700' fill='#0f172a'>%s</text>
                  <text x='120' y='410' font-family='Arial' font-size='32' fill='#475569'>Status: %s</text>
                  <text x='120' y='490' font-family='Arial' font-size='28' fill='#64748b'>This seeded record uses a generated preview placeholder.</text>
                  <rect x='120' y='590' width='960' height='620' rx='28' fill='#ecfeff' stroke='#99f6e4' stroke-width='4'/>
                  <text x='170' y='700' font-family='Arial' font-size='36' fill='#115e59'>Preview enabled</text>
                  <text x='170' y='780' font-family='Arial' font-size='28' fill='#0f172a'>New uploads will show the actual stored file.</text>
                </svg>
                """.formatted(escapeSvg(title), escapeSvg(status));
        return "data:image/svg+xml;charset=UTF-8," + URLEncoder.encode(svg, StandardCharsets.UTF_8);
    }

    private String escapeSvg(String value) {
        return StringUtils.hasText(value)
                ? value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                : "";
    }

    private String resolveFileType(MultipartFile file) {
        String text = (file.getContentType() + " " + file.getOriginalFilename()).toLowerCase();
        return text.contains("pdf") ? "PDF" : "Image";
    }

    private String storeCertificateFile(long certificateId, MultipartFile file, String normalizedType) {
        String extension = normalizedType.equals("PDF") ? ".pdf" : resolveImageExtension(file.getOriginalFilename());
        String storedFileName = "certificate-" + certificateId + "-" + UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(storedFileName).normalize();

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store certificate file", exception);
        }

        return storedFileName;
    }

    private String resolveImageExtension(String originalFilename) {
        String normalizedName = StringUtils.hasText(originalFilename) ? originalFilename.toLowerCase() : "";
        if (normalizedName.endsWith(".png")) return ".png";
        if (normalizedName.endsWith(".webp")) return ".webp";
        if (normalizedName.endsWith(".jpeg")) return ".jpeg";
        return ".jpg";
    }

    private double roundFileSize(long fileSizeBytes) {
        double sizeMb = fileSizeBytes / (1024.0 * 1024.0);
        return Math.round(sizeMb * 100.0) / 100.0;
    }

    private String buildFileUrl(Long certificateId) {
        return "/api/student/certificates/" + certificateId + "/file";
    }

    private String resolveContentType(String type) {
        return "PDF".equalsIgnoreCase(type) ? "application/pdf" : "image/jpeg";
    }

    private CertificateRecord findCertificate(Long certificateId) {
        return certificates.stream()
                .filter(record -> record.id().equals(certificateId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));
    }

    private int findCertificateIndex(Long certificateId) {
        for (int index = 0; index < certificates.size(); index += 1) {
            CertificateRecord record = certificates.get(index);
            if (record.id().equals(certificateId)) {
                return index;
            }
        }
        throw new IllegalArgumentException("Certificate not found");
    }

    private String normalizeCertificateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Certificate status is required");
        }

        return switch (status.trim().toLowerCase()) {
            case "approved", "verified" -> "Verified";
            case "rejected" -> "Rejected";
            case "pending" -> "Pending";
            default -> throw new IllegalArgumentException("Status must be approved, rejected, or pending");
        };
    }

    private String normalizeModerationRemarks(String normalizedStatus, String remarks, String existingRemarks) {
        String trimmedRemarks = remarks == null ? "" : remarks.trim();
        if (!trimmedRemarks.isEmpty()) {
            return trimmedRemarks;
        }

        return switch (normalizedStatus) {
            case "Verified" -> "Approved by admin reviewer.";
            case "Rejected" -> "Rejected by admin reviewer without additional feedback.";
            default -> existingRemarks;
        };
    }
}
