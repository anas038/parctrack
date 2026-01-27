package com.parctrack.application.dashboard;

import com.parctrack.application.dto.dashboard.DashboardSummary;
import com.parctrack.domain.equipment.*;
import com.parctrack.infrastructure.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class DashboardService {

    private final EquipmentRepository equipmentRepository;
    private final StoplightService stoplightService;

    public DashboardService(EquipmentRepository equipmentRepository, StoplightService stoplightService) {
        this.equipmentRepository = equipmentRepository;
        this.stoplightService = stoplightService;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        UUID orgId = TenantContext.getCurrentTenant();
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(15);

        long totalEquipment = equipmentRepository.countByOrganizationId(orgId);
        long overdueCount = equipmentRepository.countByOrganizationIdAndNextServiceBefore(orgId, today);
        long warningCount = equipmentRepository.countByOrganizationIdAndNextServiceBetween(orgId, today, warningDate);

        Page<Equipment> equipmentPage = equipmentRepository.findByOrganizationId(orgId, PageRequest.of(0, 5000));

        long greenCount = 0;
        long yellowCount = 0;
        long redCount = 0;

        for (Equipment equipment : equipmentPage.getContent()) {
            StoplightStatus status = stoplightService.calculateStatus(equipment);
            switch (status) {
                case GREEN -> greenCount++;
                case YELLOW -> yellowCount++;
                case RED -> redCount++;
            }
        }

        double compliancePercentage = totalEquipment > 0
                ? (greenCount * 100.0) / totalEquipment
                : 100.0;

        return new DashboardSummary(
                totalEquipment,
                greenCount,
                yellowCount,
                redCount,
                overdueCount,
                warningCount,
                Math.round(compliancePercentage * 100.0) / 100.0
        );
    }
}
