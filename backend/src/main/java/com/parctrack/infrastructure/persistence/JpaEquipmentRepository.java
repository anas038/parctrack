package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.equipment.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaEquipmentRepository extends JpaRepository<Equipment, UUID>, EquipmentRepository {

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.id = :id AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    Optional<Equipment> findByIdAndOrganizationId(@Param("id") UUID id, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.serialNumber = :serialNumber AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    Optional<Equipment> findBySerialNumberAndOrganizationId(@Param("serialNumber") String serialNumber, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT e FROM Equipment e WHERE (e.serialNumber = :query OR e.custAssetId = :query OR e.qrCodeValue = :query) AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    Optional<Equipment> findBySerialNumberOrCustAssetIdAndOrganizationId(@Param("query") String query, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.qrCodeValue = :qrCodeValue AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL ORDER BY e.createdAt ASC")
    Optional<Equipment> findByQrCodeValueAndOrganizationId(@Param("qrCodeValue") String qrCodeValue, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT e FROM Equipment e WHERE (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    Page<Equipment> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Override
    @Query("SELECT e FROM Equipment e LEFT JOIN e.site s LEFT JOIN s.customer c WHERE " +
           "(e.organization.id = :organizationId OR c.organization.id = :organizationId) AND e.deletedAt IS NULL " +
           "AND (:agreementStatus IS NULL OR e.agreementStatus = :agreementStatus) " +
           "AND (:serviceCycle IS NULL OR e.serviceCycle = :serviceCycle) " +
           "AND (:nextServiceFrom IS NULL OR e.nextService >= :nextServiceFrom) " +
           "AND (:nextServiceTo IS NULL OR e.nextService <= :nextServiceTo) " +
           "AND (:searchQuery IS NULL OR e.serialNumber LIKE %:searchQuery% OR e.custAssetId LIKE %:searchQuery% OR e.qrCodeValue LIKE %:searchQuery%) " +
           "AND (:customerId IS NULL OR c.id = :customerId) " +
           "AND (:siteId IS NULL OR s.id = :siteId) " +
           "AND (:equipmentTypeId IS NULL OR e.equipmentType.id = :equipmentTypeId) " +
           "AND (:lifecycleStatus IS NULL OR e.lifecycleStatus = :lifecycleStatus)")
    Page<Equipment> findByOrganizationIdWithFilters(
            @Param("organizationId") UUID organizationId,
            @Param("agreementStatus") AgreementStatus agreementStatus,
            @Param("serviceCycle") ServiceCycle serviceCycle,
            @Param("nextServiceFrom") LocalDate nextServiceFrom,
            @Param("nextServiceTo") LocalDate nextServiceTo,
            @Param("searchQuery") String searchQuery,
            @Param("customerId") UUID customerId,
            @Param("siteId") UUID siteId,
            @Param("equipmentTypeId") UUID equipmentTypeId,
            @Param("lifecycleStatus") LifecycleStatus lifecycleStatus,
            Pageable pageable);

    @Override
    @Query("SELECT e FROM Equipment e WHERE (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL " +
           "AND (:agreementStatus IS NULL OR e.agreementStatus = :agreementStatus) " +
           "AND (:serviceCycle IS NULL OR e.serviceCycle = :serviceCycle) " +
           "AND (:nextServiceFrom IS NULL OR e.nextService >= :nextServiceFrom) " +
           "AND (:nextServiceTo IS NULL OR e.nextService <= :nextServiceTo) " +
           "AND (:searchQuery IS NULL OR e.serialNumber LIKE %:searchQuery% OR e.custAssetId LIKE %:searchQuery%)")
    Page<Equipment> findByOrganizationIdWithFilters(
            @Param("organizationId") UUID organizationId,
            @Param("agreementStatus") AgreementStatus agreementStatus,
            @Param("serviceCycle") ServiceCycle serviceCycle,
            @Param("nextServiceFrom") LocalDate nextServiceFrom,
            @Param("nextServiceTo") LocalDate nextServiceTo,
            @Param("searchQuery") String searchQuery,
            Pageable pageable);

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.id IN :ids AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    List<Equipment> findByIdInAndOrganizationId(@Param("ids") List<UUID> ids, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.site IS NULL AND e.deletedAt IS NULL")
    List<Equipment> findBySiteIdIsNullAndDeletedAtIsNull();

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.provisional = true AND e.provisionalExpiresAt < :now AND e.deletedAt IS NULL")
    List<Equipment> findByProvisionalTrueAndProvisionalExpiresAtBefore(@Param("now") Instant now);

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.site.id = :siteId AND e.deletedAt IS NULL")
    List<Equipment> findBySiteId(@Param("siteId") UUID siteId);

    @Override
    @Query("SELECT COUNT(e) FROM Equipment e WHERE (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    long countByOrganizationId(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT COUNT(e) FROM Equipment e WHERE (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.agreementStatus = :agreementStatus AND e.deletedAt IS NULL")
    long countByOrganizationIdAndAgreementStatus(@Param("organizationId") UUID organizationId, @Param("agreementStatus") AgreementStatus agreementStatus);

    @Override
    @Query("SELECT COUNT(e) FROM Equipment e WHERE (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.nextService < :date AND e.deletedAt IS NULL")
    long countByOrganizationIdAndNextServiceBefore(@Param("organizationId") UUID organizationId, @Param("date") LocalDate date);

    @Override
    @Query("SELECT COUNT(e) FROM Equipment e WHERE (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.nextService BETWEEN :from AND :to AND e.deletedAt IS NULL")
    long countByOrganizationIdAndNextServiceBetween(@Param("organizationId") UUID organizationId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Override
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.site IS NULL AND e.deletedAt IS NULL AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId)")
    long countBySiteIdIsNullAndDeletedAtIsNullAndOrganizationId(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Equipment e WHERE e.serialNumber = :serialNumber AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    boolean existsBySerialNumberAndOrganizationId(@Param("serialNumber") String serialNumber, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Equipment e WHERE e.custAssetId = :custAssetId AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    boolean existsByCustAssetIdAndOrganizationIdAndDeletedAtIsNull(@Param("custAssetId") String custAssetId, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT e FROM Equipment e WHERE e.custAssetId = :custAssetId AND (e.organization.id = :organizationId OR e.site.customer.organization.id = :organizationId) AND e.deletedAt IS NULL")
    Optional<Equipment> findByCustAssetIdAndOrganizationIdAndDeletedAtIsNull(@Param("custAssetId") String custAssetId, @Param("organizationId") UUID organizationId);
}
