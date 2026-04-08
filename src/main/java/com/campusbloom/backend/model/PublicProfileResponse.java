package com.campusbloom.backend.model;

import java.util.List;

public record PublicProfileResponse(
        String fullName,
        String department,
        String year,
        String bio,
        boolean publicProfile,
        int extracurricularScore,
        List<DashboardMetric> stats,
        List<Achievement> achievements,
        List<TimelineItem> timeline,
        List<Badge> badges
) {
}
