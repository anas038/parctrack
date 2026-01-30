package com.parctrack.application.dto.equipment;

import java.util.List;

public record EquipmentHistoryDto(
        List<ServiceRecordDto> recentServices,
        List<MonthlySummary> monthlySummaries
) {
    public record MonthlySummary(
            String month,
            long serviceCount
    ) {}
}
