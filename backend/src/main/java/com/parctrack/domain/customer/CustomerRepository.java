package com.parctrack.domain.customer;

import com.parctrack.domain.equipment.AgreementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Page<Customer> findByOrganizationId(UUID organizationId, Pageable pageable);
    Page<Customer> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);
    List<Customer> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
    List<Customer> findByAgreementStatusAndContractEndDateBefore(AgreementStatus status, LocalDate date);
    long countByOrganizationId(UUID organizationId);
    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
    void deleteById(UUID id);
}
