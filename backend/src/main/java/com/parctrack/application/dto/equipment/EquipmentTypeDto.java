package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.EquipmentType;
import java.time.Instant;
import java.util.UUID;

public record EquipmentTypeDto(
        UUID id,
        String name,
        String description,
        int displayOrder,
        boolean active,
        Instant createdAt
) {
    public static EquipmentTypeDto from(EquipmentType equipmentType) {
        return new EquipmentTypeDto(
                equipmentType.getId(),
                equipmentType.getName(),
                equipmentType.getDescription(),
                equipmentType.getDisplayOrder(),
                equipmentType.isActive(),
                equipmentType.getCreatedAt()
        );
    }
}
