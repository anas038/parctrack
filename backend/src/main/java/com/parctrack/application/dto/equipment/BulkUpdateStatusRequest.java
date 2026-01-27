package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.AgreementStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record BulkUpdateStatusRequest(
        @NotEmpty(message = "At least one equipment ID is required")
        List<UUID> ids,

        @NotNull(message = "Agreement status is required")
        AgreementStatus agreementStatus
) {}
