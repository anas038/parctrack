package com.parctrack.application.customer;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.customer.*;
import com.parctrack.domain.customer.Customer;
import com.parctrack.domain.customer.CustomerRepository;
import com.parctrack.domain.equipment.EquipmentRepository;
import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.organization.OrganizationRepository;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final EquipmentRepository equipmentRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    public CustomerService(
            CustomerRepository customerRepository,
            SiteRepository siteRepository,
            EquipmentRepository equipmentRepository,
            OrganizationRepository organizationRepository,
            AuditService auditService) {
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.equipmentRepository = equipmentRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public CustomerDto getById(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Customer customer = customerRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return CustomerDto.from(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> list(int page, int size, String sortBy, String sortDirection) {
        UUID orgId = TenantContext.getCurrentTenant();
        Sort sort = Sort.by(
                sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        return customerRepository.findByOrganizationIdAndDeletedAtIsNull(orgId, pageable)
                .map(CustomerDto::from);
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> listAll() {
        UUID orgId = TenantContext.getCurrentTenant();
        return customerRepository.findByOrganizationIdAndDeletedAtIsNull(orgId)
                .stream()
                .map(CustomerDto::from)
                .toList();
    }

    @Transactional
    public CustomerDto create(CreateCustomerRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();

        if (customerRepository.existsByNameAndOrganizationId(request.name(), orgId)) {
            throw new BusinessException("Customer with this name already exists");
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Customer customer = new Customer(organization, request.name(), request.agreementStatus());
        customer.setContractEndDate(request.contractEndDate());

        customer = customerRepository.save(customer);
        auditService.logAction("CUSTOMER_CREATED", "Customer", customer.getId());

        return CustomerDto.from(customer);
    }

    @Transactional
    public CustomerDto update(UUID id, UpdateCustomerRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        Customer customer = customerRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (request.name() != null && !request.name().equals(customer.getName())) {
            if (customerRepository.existsByNameAndOrganizationId(request.name(), orgId)) {
                throw new BusinessException("Customer with this name already exists");
            }
            customer.setName(request.name());
        }
        if (request.agreementStatus() != null) {
            customer.setAgreementStatus(request.agreementStatus());
        }
        if (request.contractEndDate() != null) {
            customer.setContractEndDate(request.contractEndDate());
        }

        customer = customerRepository.save(customer);
        auditService.logAction("CUSTOMER_UPDATED", "Customer", customer.getId());

        return CustomerDto.from(customer);
    }

    @Transactional
    public void delete(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        Customer customer = customerRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Soft delete the customer
        customer.softDelete();
        customerRepository.save(customer);

        // Soft delete all sites and orphan their equipment
        List<Site> sites = siteRepository.findByCustomerIdAndDeletedAtIsNull(id);
        for (Site site : sites) {
            site.softDelete();
            siteRepository.save(site);

            // Orphan equipment by setting site to null
            var equipmentList = equipmentRepository.findBySiteId(site.getId());
            for (var equipment : equipmentList) {
                equipment.setSite(null);
                equipmentRepository.save(equipment);
            }
        }

        auditService.logAction("CUSTOMER_DELETED", "Customer", customer.getId(),
                "Cascaded soft-delete to " + sites.size() + " sites");
    }
}
