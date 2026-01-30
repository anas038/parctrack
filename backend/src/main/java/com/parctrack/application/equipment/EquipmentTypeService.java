package com.parctrack.application.equipment;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.equipment.*;
import com.parctrack.domain.equipment.EquipmentType;
import com.parctrack.domain.equipment.EquipmentTypeRepository;
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
public class EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    public EquipmentTypeService(
            EquipmentTypeRepository equipmentTypeRepository,
            OrganizationRepository organizationRepository,
            AuditService auditService) {
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public EquipmentTypeDto getById(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        EquipmentType equipmentType = equipmentTypeRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));
        return EquipmentTypeDto.from(equipmentType);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentTypeDto> list(int page, int size, String sortBy, String sortDirection) {
        UUID orgId = TenantContext.getCurrentTenant();
        Sort sort = Sort.by(
                sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        return equipmentTypeRepository.findByOrganizationId(orgId, pageable)
                .map(EquipmentTypeDto::from);
    }

    @Transactional(readOnly = true)
    public List<EquipmentTypeDto> listAll() {
        UUID orgId = TenantContext.getCurrentTenant();
        return equipmentTypeRepository.findByOrganizationIdOrderByDisplayOrderAsc(orgId)
                .stream()
                .map(EquipmentTypeDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentTypeDto> listActive() {
        UUID orgId = TenantContext.getCurrentTenant();
        return equipmentTypeRepository.findByOrganizationIdAndActiveTrueOrderByDisplayOrderAsc(orgId)
                .stream()
                .map(EquipmentTypeDto::from)
                .toList();
    }

    @Transactional
    public EquipmentTypeDto create(CreateEquipmentTypeRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();

        if (equipmentTypeRepository.existsByNameAndOrganizationId(request.name(), orgId)) {
            throw new BusinessException("Equipment type with this name already exists");
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        EquipmentType equipmentType = new EquipmentType(organization, request.name());
        equipmentType.setDescription(request.description());

        if (request.displayOrder() != null) {
            equipmentType.setDisplayOrder(request.displayOrder());
        } else {
            // Auto-assign display order as max + 1
            long count = equipmentTypeRepository.countByOrganizationId(orgId);
            equipmentType.setDisplayOrder((int) count);
        }

        equipmentType = equipmentTypeRepository.save(equipmentType);
        auditService.logAction("EQUIPMENT_TYPE_CREATED", "EquipmentType", equipmentType.getId());

        return EquipmentTypeDto.from(equipmentType);
    }

    @Transactional
    public EquipmentTypeDto update(UUID id, UpdateEquipmentTypeRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        EquipmentType equipmentType = equipmentTypeRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));

        if (request.name() != null && !request.name().equals(equipmentType.getName())) {
            if (equipmentTypeRepository.existsByNameAndOrganizationId(request.name(), orgId)) {
                throw new BusinessException("Equipment type with this name already exists");
            }
            equipmentType.setName(request.name());
        }
        if (request.description() != null) {
            equipmentType.setDescription(request.description());
        }
        if (request.displayOrder() != null) {
            equipmentType.setDisplayOrder(request.displayOrder());
        }
        if (request.active() != null) {
            equipmentType.setActive(request.active());
        }

        equipmentType = equipmentTypeRepository.save(equipmentType);
        auditService.logAction("EQUIPMENT_TYPE_UPDATED", "EquipmentType", equipmentType.getId());

        return EquipmentTypeDto.from(equipmentType);
    }

    @Transactional
    public void reorder(ReorderEquipmentTypesRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();

        List<UUID> orderedIds = request.orderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            EquipmentType equipmentType = equipmentTypeRepository.findByIdAndOrganizationId(orderedIds.get(i), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));
            equipmentType.setDisplayOrder(i);
            equipmentTypeRepository.save(equipmentType);
        }

        auditService.logAction("EQUIPMENT_TYPES_REORDERED", "EquipmentType", null,
                "Reordered " + orderedIds.size() + " equipment types");
    }

    @Transactional
    public void delete(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        EquipmentType equipmentType = equipmentTypeRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));

        equipmentTypeRepository.deleteById(id);
        auditService.logAction("EQUIPMENT_TYPE_DELETED", "EquipmentType", id);
    }
}
