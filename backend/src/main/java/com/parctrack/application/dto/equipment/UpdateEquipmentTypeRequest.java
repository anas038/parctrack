package com.parctrack.application.dto.equipment;

public record UpdateEquipmentTypeRequest(
        String name,
        String description,
        Integer displayOrder,
        Boolean active
) {}
