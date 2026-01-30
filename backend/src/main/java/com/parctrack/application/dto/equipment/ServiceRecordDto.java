package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.ReasonCode;
import com.parctrack.domain.equipment.ServiceRecord;
import java.time.Instant;
import java.util.UUID;

public record ServiceRecordDto(
        UUID id,
        UUID equipmentId,
        Instant servicedAt,
        UUID servicedByUserId,
        String servicedByName,
        ReasonCode reasonCode,
        Instant createdAt
) {
    public static ServiceRecordDto from(ServiceRecord serviceRecord) {
        return new ServiceRecordDto(
                serviceRecord.getId(),
                serviceRecord.getEquipment().getId(),
                serviceRecord.getServicedAt(),
                serviceRecord.getServicedBy().getId(),
                serviceRecord.getServicedBy().getUsername(),
                serviceRecord.getReasonCode(),
                serviceRecord.getCreatedAt()
        );
    }
}
