package com.parctrack.application.dto.equipment;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record BulkDeleteRequest(
        @NotEmpty(message = "At least one equipment ID is required")
        List<UUID> ids
) {}
