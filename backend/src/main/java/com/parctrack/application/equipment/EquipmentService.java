package com.parctrack.application.equipment;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.equipment.*;
import com.parctrack.domain.equipment.*;
import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.organization.OrganizationRepository;
import com.parctrack.domain.site.Site;
import com.parctrack.domain.site.SiteRepository;
import com.parctrack.domain.user.User;
import com.parctrack.domain.user.UserRepository;
import com.parctrack.infrastructure.security.TenantContext;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.BusinessException;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final OrganizationRepository organizationRepository;
    private final SiteRepository siteRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final UserRepository userRepository;
    private final StoplightService stoplightService;
    private final AuditService auditService;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            OrganizationRepository organizationRepository,
            SiteRepository siteRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            ServiceRecordRepository serviceRecordRepository,
            UserRepository userRepository,
            StoplightService stoplightService,
            AuditService auditService) {
        this.equipmentRepository = equipmentRepository;
        this.organizationRepository = organizationRepository;
        this.siteRepository = siteRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.serviceRecordRepository = serviceRecordRepository;
        this.userRepository = userRepository;
        this.stoplightService = stoplightService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public EquipmentDto lookup(String query) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findBySerialNumberOrCustAssetIdAndOrganizationId(query, orgId)
                .or(() -> equipmentRepository.findByQrCodeValueAndOrganizationId(query, orgId))
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + query));
        return toDto(equipment);
    }

    @Transactional(readOnly = true)
    public EquipmentDto getById(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));
        return toDto(equipment);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentDto> list(EquipmentFilterRequest filter) {
        UUID orgId = TenantContext.getCurrentTenant();
        Sort sort = Sort.by(
                filter.sortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.sortBy()
        );
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        Page<Equipment> page = equipmentRepository.findByOrganizationIdWithFilters(
                orgId,
                filter.agreementStatus(),
                filter.serviceCycle(),
                filter.nextServiceFrom(),
                filter.nextServiceTo(),
                filter.searchQuery(),
                filter.customerId(),
                filter.siteId(),
                filter.equipmentTypeId(),
                filter.lifecycleStatus(),
                pageable
        );

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentDto> findOrphanedEquipment(int page, int size) {
        UUID orgId = TenantContext.getCurrentTenant();
        Pageable pageable = PageRequest.of(page, size, Sort.by("serialNumber"));

        // Find equipment with no site
        Page<Equipment> orphaned = equipmentRepository.findByOrganizationIdWithFilters(
                orgId, null, null, null, null, null, null, null, null, null, pageable
        );

        // Filter to only orphaned equipment
        return orphaned.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long countOrphanedEquipment() {
        UUID orgId = TenantContext.getCurrentTenant();
        return equipmentRepository.countBySiteIdIsNullAndDeletedAtIsNullAndOrganizationId(orgId);
    }

    @Transactional
    public EquipmentDto create(CreateEquipmentRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();

        if (equipmentRepository.existsBySerialNumberAndOrganizationId(request.serialNumber(), orgId)) {
            throw new BusinessException("Equipment with this serial number already exists");
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Equipment equipment = new Equipment(
                organization,
                request.serialNumber(),
                request.agreementStatus(),
                request.serviceCycle()
        );
        equipment.setCustAssetId(request.custAssetId());
        if (request.nextService() != null) {
            equipment.setNextService(request.nextService());
            equipment.setNextServiceOverride(true);
        }

        equipment = equipmentRepository.save(equipment);
        auditService.logAction("EQUIPMENT_CREATED", "Equipment", equipment.getId());

        return toDto(equipment);
    }

    @Transactional
    public EquipmentDto update(UUID id, UpdateEquipmentRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (request.custAssetId() != null) {
            // Handle asset ID collision
            if (!request.custAssetId().equals(equipment.getCustAssetId())) {
                Optional<Equipment> existing = equipmentRepository.findByCustAssetIdAndOrganizationIdAndDeletedAtIsNull(
                        request.custAssetId(), orgId);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    // Soft delete the old record and link via predecessor_id
                    Equipment oldEquipment = existing.get();
                    oldEquipment.softDelete();
                    equipmentRepository.save(oldEquipment);
                    equipment.setPredecessorId(oldEquipment.getId());
                }
            }
            equipment.setCustAssetId(request.custAssetId());
        }
        if (request.agreementStatus() != null) {
            equipment.setAgreementStatus(request.agreementStatus());
        }
        if (request.serviceCycle() != null) {
            equipment.setServiceCycle(request.serviceCycle());
        }
        if (request.nextService() != null) {
            equipment.setNextService(request.nextService());
            equipment.setNextServiceOverride(request.nextServiceOverride() != null ? request.nextServiceOverride() : true);
        }

        equipment = equipmentRepository.save(equipment);
        auditService.logAction("EQUIPMENT_UPDATED", "Equipment", equipment.getId());

        return toDto(equipment);
    }

    @Transactional
    public ServiceRecordDto markServiced(UUID id, MarkServicedRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Calculate current status
        StoplightStatus currentStatus = stoplightService.calculateStatus(equipment);

        // Require reason code if equipment is RED
        if (currentStatus == StoplightStatus.RED && (request == null || request.reasonCode() == null)) {
            throw new BusinessException("Reason code is required when servicing equipment with RED status");
        }

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User servicedBy = userRepository.findByEmailAndOrganizationId(username, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Create service record
        ReasonCode reasonCode = request != null ? request.reasonCode() : null;
        ServiceRecord serviceRecord = new ServiceRecord(equipment, servicedBy, reasonCode);
        serviceRecord = serviceRecordRepository.save(serviceRecord);

        // Update equipment
        equipment.markServiced();
        equipmentRepository.save(equipment);

        auditService.logAction("EQUIPMENT_SERVICED", "Equipment", equipment.getId(),
                reasonCode != null ? "Reason: " + reasonCode : null);

        return ServiceRecordDto.from(serviceRecord);
    }

    @Transactional(readOnly = true)
    public EquipmentHistoryDto getHistory(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Get all service records ordered by date descending
        List<ServiceRecord> allRecords = serviceRecordRepository.findByEquipmentIdOrderByServicedAtDesc(id);

        // Tiered history: last 1 year detailed, older grouped by month
        Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);

        List<ServiceRecordDto> recentServices = allRecords.stream()
                .filter(sr -> sr.getServicedAt().isAfter(oneYearAgo))
                .map(ServiceRecordDto::from)
                .toList();

        // Group older records by month
        Map<YearMonth, Long> monthlyCounts = allRecords.stream()
                .filter(sr -> !sr.getServicedAt().isAfter(oneYearAgo))
                .collect(Collectors.groupingBy(
                        sr -> YearMonth.from(sr.getServicedAt().atZone(java.time.ZoneId.systemDefault())),
                        Collectors.counting()
                ));

        List<EquipmentHistoryDto.MonthlySummary> monthlySummaries = monthlyCounts.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, Long>comparingByKey().reversed())
                .map(entry -> new EquipmentHistoryDto.MonthlySummary(
                        entry.getKey().toString(),
                        entry.getValue()
                ))
                .toList();

        return new EquipmentHistoryDto(recentServices, monthlySummaries);
    }

    @Transactional
    public void delete(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        equipment.softDelete();
        equipmentRepository.save(equipment);
        auditService.logAction("EQUIPMENT_DELETED", "Equipment", equipment.getId());
    }

    @Transactional
    public BulkOperationResult bulkDelete(BulkDeleteRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        List<Equipment> equipmentList = equipmentRepository.findByIdInAndOrganizationId(request.ids(), orgId);

        int successCount = 0;
        for (Equipment equipment : equipmentList) {
            equipment.softDelete();
            equipmentRepository.save(equipment);
            successCount++;
        }

        auditService.logAction("EQUIPMENT_BULK_DELETE", "Equipment", null, "Deleted " + successCount + " items");

        return new BulkOperationResult(
                successCount,
                request.ids().size() - successCount,
                "Successfully deleted " + successCount + " equipment"
        );
    }

    @Transactional
    public BulkOperationResult bulkUpdateStatus(BulkUpdateStatusRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        List<Equipment> equipmentList = equipmentRepository.findByIdInAndOrganizationId(request.ids(), orgId);

        int successCount = 0;
        for (Equipment equipment : equipmentList) {
            equipment.setAgreementStatus(request.agreementStatus());
            equipmentRepository.save(equipment);
            successCount++;
        }

        auditService.logAction("EQUIPMENT_BULK_UPDATE_STATUS", "Equipment", null,
                "Updated " + successCount + " items to " + request.agreementStatus());

        return new BulkOperationResult(
                successCount,
                request.ids().size() - successCount,
                "Successfully updated " + successCount + " equipment"
        );
    }

    @Transactional
    public BulkOperationResult bulkUpdateCycle(BulkUpdateCycleRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        List<Equipment> equipmentList = equipmentRepository.findByIdInAndOrganizationId(request.ids(), orgId);

        int successCount = 0;
        for (Equipment equipment : equipmentList) {
            equipment.setServiceCycle(request.serviceCycle());
            equipmentRepository.save(equipment);
            successCount++;
        }

        auditService.logAction("EQUIPMENT_BULK_UPDATE_CYCLE", "Equipment", null,
                "Updated " + successCount + " items to " + request.serviceCycle());

        return new BulkOperationResult(
                successCount,
                request.ids().size() - successCount,
                "Successfully updated " + successCount + " equipment"
        );
    }

    private EquipmentDto toDto(Equipment equipment) {
        StoplightStatus status = stoplightService.calculateStatus(equipment);
        return EquipmentDto.from(equipment, status);
    }
}
