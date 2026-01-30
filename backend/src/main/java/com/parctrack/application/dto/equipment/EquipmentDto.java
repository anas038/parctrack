package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentDto(
        UUID id,
        String serialNumber,
        String custAssetId,
        String qrCodeValue,
        AgreementStatus agreementStatus,
        LifecycleStatus lifecycleStatus,
        ServiceCycle serviceCycle,
        Instant lastService,
        LocalDate nextService,
        boolean nextServiceOverride,
        StoplightStatus stoplightStatus,
        boolean provisional,
        Instant provisionalExpiresAt,
        UUID predecessorId,
        SiteInfo site,
        EquipmentTypeInfo equipmentType,
        Instant createdAt,
        Instant updatedAt
) {
    public record SiteInfo(
            UUID id,
            String name,
            CustomerInfo customer
    ) {}

    public record CustomerInfo(
            UUID id,
            String name,
            AgreementStatus agreementStatus
    ) {}

    public record EquipmentTypeInfo(
            UUID id,
            String name
    ) {}

    public static EquipmentDto from(Equipment equipment, StoplightStatus stoplightStatus) {
        SiteInfo siteInfo = null;
        if (equipment.getSite() != null) {
            var customer = equipment.getSite().getCustomer();
            CustomerInfo customerInfo = new CustomerInfo(
                    customer.getId(),
                    customer.getName(),
                    customer.getAgreementStatus()
            );
            siteInfo = new SiteInfo(
                    equipment.getSite().getId(),
                    equipment.getSite().getName(),
                    customerInfo
            );
        }

        EquipmentTypeInfo typeInfo = null;
        if (equipment.getEquipmentType() != null) {
            typeInfo = new EquipmentTypeInfo(
                    equipment.getEquipmentType().getId(),
                    equipment.getEquipmentType().getName()
            );
        }

        return new EquipmentDto(
                equipment.getId(),
                equipment.getSerialNumber(),
                equipment.getCustAssetId(),
                equipment.getQrCodeValue(),
                equipment.getAgreementStatus(),
                equipment.getLifecycleStatus(),
                equipment.getServiceCycle(),
                equipment.getLastService(),
                equipment.getNextService(),
                equipment.isNextServiceOverride(),
                stoplightStatus,
                equipment.isProvisional(),
                equipment.getProvisionalExpiresAt(),
                equipment.getPredecessorId(),
                siteInfo,
                typeInfo,
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
    }
}
