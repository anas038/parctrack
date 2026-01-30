package com.parctrack.domain.equipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepository {
    Equipment save(Equipment equipment);
    Optional<Equipment> findById(UUID id);
    Optional<Equipment> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Optional<Equipment> findBySerialNumberAndOrganizationId(String serialNumber, UUID organizationId);
    Optional<Equipment> findBySerialNumberOrCustAssetIdAndOrganizationId(String query, UUID organizationId);
    Optional<Equipment> findByQrCodeValueAndOrganizationId(String qrCodeValue, UUID organizationId);
    Page<Equipment> findByOrganizationId(UUID organizationId, Pageable pageable);
    Page<Equipment> findByOrganizationIdWithFilters(
            UUID organizationId,
            AgreementStatus agreementStatus,
            ServiceCycle serviceCycle,
            LocalDate nextServiceFrom,
            LocalDate nextServiceTo,
            String searchQuery,
            UUID customerId,
            UUID siteId,
            UUID equipmentTypeId,
            LifecycleStatus lifecycleStatus,
            Pageable pageable);
    Page<Equipment> findByOrganizationIdWithFilters(
            UUID organizationId,
            AgreementStatus agreementStatus,
            ServiceCycle serviceCycle,
            LocalDate nextServiceFrom,
            LocalDate nextServiceTo,
            String searchQuery,
            Pageable pageable);
    List<Equipment> findByIdInAndOrganizationId(List<UUID> ids, UUID organizationId);
    List<Equipment> findBySiteIdIsNullAndDeletedAtIsNull();
    List<Equipment> findByProvisionalTrueAndProvisionalExpiresAtBefore(Instant now);
    List<Equipment> findBySiteId(UUID siteId);
    long countByOrganizationId(UUID organizationId);
    long countByOrganizationIdAndAgreementStatus(UUID organizationId, AgreementStatus agreementStatus);
    long countByOrganizationIdAndNextServiceBefore(UUID organizationId, LocalDate date);
    long countByOrganizationIdAndNextServiceBetween(UUID organizationId, LocalDate from, LocalDate to);
    long countBySiteIdIsNullAndDeletedAtIsNullAndOrganizationId(UUID organizationId);
    boolean existsBySerialNumberAndOrganizationId(String serialNumber, UUID organizationId);
    boolean existsByCustAssetIdAndOrganizationIdAndDeletedAtIsNull(String custAssetId, UUID organizationId);
    Optional<Equipment> findByCustAssetIdAndOrganizationIdAndDeletedAtIsNull(String custAssetId, UUID organizationId);
    void deleteById(UUID id);
}
