package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.AgreementStatus;
import com.parctrack.domain.equipment.ServiceCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateEquipmentRequest(
        @NotBlank(message = "Serial number is required")
        String serialNumber,

        String custAssetId,

        @NotNull(message = "Agreement status is required")
        AgreementStatus agreementStatus,

        @NotNull(message = "Service cycle is required")
        ServiceCycle serviceCycle,

        LocalDate nextService
) {}
