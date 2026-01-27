package com.parctrack.application.dto.dashboard;

public record DashboardSummary(
        long totalEquipment,
        long greenCount,
        long yellowCount,
        long redCount,
        long overdueCount,
        long warningCount,
        double compliancePercentage
) {}
