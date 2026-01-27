package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.AgreementStatus;
import com.parctrack.domain.equipment.ServiceCycle;
import java.time.LocalDate;

public record UpdateEquipmentRequest(
        String custAssetId,
        AgreementStatus agreementStatus,
        ServiceCycle serviceCycle,
        LocalDate nextService,
        Boolean nextServiceOverride
) {}
