package com.parctrack.domain.site;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteRepository {
    Site save(Site site);
    Optional<Site> findById(UUID id);
    Optional<Site> findByIdAndCustomerOrganizationId(UUID id, UUID organizationId);
    Page<Site> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Site> findByCustomerIdAndDeletedAtIsNull(UUID customerId, Pageable pageable);
    List<Site> findByCustomerIdAndDeletedAtIsNull(UUID customerId);
    Page<Site> findByCustomerOrganizationId(UUID organizationId, Pageable pageable);
    Page<Site> findByCustomerOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);
    List<Site> findByCustomerOrganizationIdAndDeletedAtIsNull(UUID organizationId);
    long countByCustomerId(UUID customerId);
    boolean existsByNameAndCustomerId(String name, UUID customerId);
    void deleteById(UUID id);
}
