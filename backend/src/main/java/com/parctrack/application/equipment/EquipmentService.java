package com.parctrack.application.equipment;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.equipment.*;
import com.parctrack.domain.equipment.*;
import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.organization.OrganizationRepository;
import com.parctrack.infrastructure.security.TenantContext;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.BusinessException;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final OrganizationRepository organizationRepository;
    private final StoplightService stoplightService;
    private final AuditService auditService;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            OrganizationRepository organizationRepository,
            StoplightService stoplightService,
            AuditService auditService) {
        this.equipmentRepository = equipmentRepository;
        this.organizationRepository = organizationRepository;
        this.stoplightService = stoplightService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public EquipmentDto lookup(String query) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findBySerialNumberOrCustAssetIdAndOrganizationId(query, orgId)
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
                pageable
        );

        return page.map(this::toDto);
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
    public void markServiced(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Equipment equipment = equipmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        equipment.markServiced();
        equipmentRepository.save(equipment);
        auditService.logAction("EQUIPMENT_SERVICED", "Equipment", equipment.getId());
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
