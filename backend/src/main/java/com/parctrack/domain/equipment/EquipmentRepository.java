package com.parctrack.domain.equipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepository {
    Equipment save(Equipment equipment);
    List<Equipment> saveAll(List<Equipment> equipment);
    Optional<Equipment> findById(UUID id);
    Optional<Equipment> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Optional<Equipment> findBySerialNumberAndOrganizationId(String serialNumber, UUID organizationId);
    Optional<Equipment> findBySerialNumberOrCustAssetIdAndOrganizationId(String query, UUID organizationId);
    Page<Equipment> findByOrganizationId(UUID organizationId, Pageable pageable);
    Page<Equipment> findByOrganizationIdWithFilters(
            UUID organizationId,
            AgreementStatus agreementStatus,
            ServiceCycle serviceCycle,
            LocalDate nextServiceFrom,
            LocalDate nextServiceTo,
            String searchQuery,
            Pageable pageable);
    List<Equipment> findByIdInAndOrganizationId(List<UUID> ids, UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    long countByOrganizationIdAndAgreementStatus(UUID organizationId, AgreementStatus agreementStatus);
    long countByOrganizationIdAndNextServiceBefore(UUID organizationId, LocalDate date);
    long countByOrganizationIdAndNextServiceBetween(UUID organizationId, LocalDate from, LocalDate to);
    boolean existsBySerialNumberAndOrganizationId(String serialNumber, UUID organizationId);
    void deleteById(UUID id);
}
