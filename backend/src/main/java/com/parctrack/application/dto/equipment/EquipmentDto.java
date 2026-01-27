package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentDto(
        UUID id,
        String serialNumber,
        String custAssetId,
        AgreementStatus agreementStatus,
        ServiceCycle serviceCycle,
        Instant lastService,
        LocalDate nextService,
        boolean nextServiceOverride,
        StoplightStatus stoplightStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static EquipmentDto from(Equipment equipment, StoplightStatus stoplightStatus) {
        return new EquipmentDto(
                equipment.getId(),
                equipment.getSerialNumber(),
                equipment.getCustAssetId(),
                equipment.getAgreementStatus(),
                equipment.getServiceCycle(),
                equipment.getLastService(),
                equipment.getNextService(),
                equipment.isNextServiceOverride(),
                stoplightStatus,
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
    }
}
