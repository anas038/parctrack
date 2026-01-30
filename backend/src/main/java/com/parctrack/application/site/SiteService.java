package com.parctrack.application.site;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.site.*;
import com.parctrack.domain.customer.Customer;
import com.parctrack.domain.customer.CustomerRepository;
import com.parctrack.domain.equipment.EquipmentRepository;
import com.parctrack.domain.site.Site;
import com.parctrack.domain.site.SiteRepository;
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
public class SiteService {

    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;
    private final EquipmentRepository equipmentRepository;
    private final AuditService auditService;

    public SiteService(
            SiteRepository siteRepository,
            CustomerRepository customerRepository,
            EquipmentRepository equipmentRepository,
            AuditService auditService) {
        this.siteRepository = siteRepository;
        this.customerRepository = customerRepository;
        this.equipmentRepository = equipmentRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SiteDto getById(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Site site = siteRepository.findByIdAndCustomerOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));
        return SiteDto.from(site);
    }

    @Transactional(readOnly = true)
    public Page<SiteDto> list(UUID customerId, int page, int size, String sortBy, String sortDirection) {
        UUID orgId = TenantContext.getCurrentTenant();
        Sort sort = Sort.by(
                sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        if (customerId != null) {
            // Validate customer belongs to organization
            customerRepository.findByIdAndOrganizationId(customerId, orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            return siteRepository.findByCustomerIdAndDeletedAtIsNull(customerId, pageable)
                    .map(SiteDto::from);
        }

        return siteRepository.findByCustomerOrganizationIdAndDeletedAtIsNull(orgId, pageable)
                .map(SiteDto::from);
    }

    @Transactional(readOnly = true)
    public List<SiteDto> listAll(UUID customerId) {
        UUID orgId = TenantContext.getCurrentTenant();

        if (customerId != null) {
            customerRepository.findByIdAndOrganizationId(customerId, orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            return siteRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                    .stream()
                    .map(SiteDto::from)
                    .toList();
        }

        return siteRepository.findByCustomerOrganizationIdAndDeletedAtIsNull(orgId)
                .stream()
                .map(SiteDto::from)
                .toList();
    }

    @Transactional
    public SiteDto create(CreateSiteRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();

        Customer customer = customerRepository.findByIdAndOrganizationId(request.customerId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (siteRepository.existsByNameAndCustomerId(request.name(), request.customerId())) {
            throw new BusinessException("Site with this name already exists for this customer");
        }

        Site site = new Site(customer, request.name());
        site.setAddress(request.address());
        site.setContactName(request.contactName());
        site.setContactPhone(request.contactPhone());
        if (request.metadata() != null) {
            site.setMetadata(request.metadata());
        }

        site = siteRepository.save(site);
        auditService.logAction("SITE_CREATED", "Site", site.getId());

        return SiteDto.from(site);
    }

    @Transactional
    public SiteDto update(UUID id, UpdateSiteRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        Site site = siteRepository.findByIdAndCustomerOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        if (request.customerId() != null && !request.customerId().equals(site.getCustomer().getId())) {
            Customer newCustomer = customerRepository.findByIdAndOrganizationId(request.customerId(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            site.setCustomer(newCustomer);
        }

        if (request.name() != null && !request.name().equals(site.getName())) {
            if (siteRepository.existsByNameAndCustomerId(request.name(), site.getCustomer().getId())) {
                throw new BusinessException("Site with this name already exists for this customer");
            }
            site.setName(request.name());
        }

        if (request.address() != null) {
            site.setAddress(request.address());
        }
        if (request.contactName() != null) {
            site.setContactName(request.contactName());
        }
        if (request.contactPhone() != null) {
            site.setContactPhone(request.contactPhone());
        }
        if (request.metadata() != null) {
            site.setMetadata(request.metadata());
        }

        site = siteRepository.save(site);
        auditService.logAction("SITE_UPDATED", "Site", site.getId());

        return SiteDto.from(site);
    }

    @Transactional
    public void delete(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Site site = siteRepository.findByIdAndCustomerOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        // Soft delete the site
        site.softDelete();
        siteRepository.save(site);

        // Orphan equipment by setting site to null
        var equipmentList = equipmentRepository.findBySiteId(id);
        for (var equipment : equipmentList) {
            equipment.setSite(null);
            equipmentRepository.save(equipment);
        }

        auditService.logAction("SITE_DELETED", "Site", site.getId(),
                "Orphaned " + equipmentList.size() + " equipment");
    }
}
