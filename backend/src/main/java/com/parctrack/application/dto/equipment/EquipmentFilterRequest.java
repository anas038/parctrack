package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.AgreementStatus;
import com.parctrack.domain.equipment.ServiceCycle;
import java.time.LocalDate;

public record EquipmentFilterRequest(
        AgreementStatus agreementStatus,
        ServiceCycle serviceCycle,
        LocalDate nextServiceFrom,
        LocalDate nextServiceTo,
        String searchQuery,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public EquipmentFilterRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        if (sortBy == null) sortBy = "serialNumber";
        if (sortDirection == null) sortDirection = "asc";
    }
}
