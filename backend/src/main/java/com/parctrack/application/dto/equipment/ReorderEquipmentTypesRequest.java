package com.parctrack.application.dto.equipment;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record ReorderEquipmentTypesRequest(
        @NotEmpty(message = "Order list is required")
        List<UUID> orderedIds
) {}
