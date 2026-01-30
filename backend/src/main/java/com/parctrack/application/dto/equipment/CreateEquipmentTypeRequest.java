package com.parctrack.application.dto.equipment;

import jakarta.validation.constraints.NotBlank;

public record CreateEquipmentTypeRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        Integer displayOrder
) {}
