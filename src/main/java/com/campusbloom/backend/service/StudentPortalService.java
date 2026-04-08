package com.campusbloom.backend.service;

import com.campusbloom.backend.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StudentPortalService {

    private final AtomicLong certificateIdSequence = new AtomicLong(100);
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

    private final List<CertificateRecord> certificates = new ArrayList<>(List.of(
            new CertificateRecord(1L, "Smart Campus IoT Hackathon Winner", "National Innovation Hackathon", "2026-02-15",
                    "Technical", "PDF", 1.8, "Verified", "/student-achievements",
                    "Verified by Innovation Cell and faculty mentor.", "pdf"),
            new CertificateRecord(2L, "Intercollege Basketball Tournament", "Sports Council Meet", "2026-01-29",
                    "Sports", "Image", 2.4, "Verified", "/student-achievements",
                    "Uploaded scorecard and participation certificate validated.", "image"),
            new CertificateRecord(3L, "State Cultural Fusion Performance", "Kala Utsav", "2026-01-17",
                    "Cultural", "PDF", 1.3, "Pending", "/student-achievements",
                    "Awaiting department coordinator verification remarks.", "pdf"),
            new CertificateRecord(4L, "Student Council Event Lead", "Annual Tech-Cultural Fest", "2025-12-22",
                    "Leadership", "Image", 3.1, "Verified", "/student-achievements",
                    "Committee approval verified with organizing team signatures.", "image")
    ));

    private final List<Badge> badges = List.of(
            new Badge("Gold - Innovation", "gold"),
            new Badge("Silver - Leadership", "silver"),
            new Badge("Bronze - Consistency", "bronze")
    );

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

    public CertificateRecord uploadCertificate(String fileName, String fileType, double sizeMB) {
        String normalizedType = fileType != null && fileType.equalsIgnoreCase("pdf") ? "PDF" : "Image";
        String previewKind = normalizedType.equals("PDF") ? "pdf" : "image";
        CertificateRecord record = new CertificateRecord(
                certificateIdSequence.incrementAndGet(),
                fileName,
                "Manual Upload",
                LocalDate.now().toString(),
                "Technical",
                normalizedType,
                sizeMB,
                "Pending",
                "/student-achievements",
                "Awaiting admin review and verification remarks.",
                previewKind
        );
        certificates.add(0, record);
        return record;
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
}
