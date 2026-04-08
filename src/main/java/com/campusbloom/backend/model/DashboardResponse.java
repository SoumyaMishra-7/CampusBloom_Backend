package com.campusbloom.backend.model;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        String studentName,
        int extracurricularScore,
        int portfolioCompletion,
        List<DashboardMetric> stats,
        Map<String, Integer> categoryCounts,
        List<Achievement> achievements,
        List<TimelineItem> timeline,
        List<NotificationItem> notifications,
        List<String> spotlightSkills,
        List<QuickAction> quickActions
) {
}
