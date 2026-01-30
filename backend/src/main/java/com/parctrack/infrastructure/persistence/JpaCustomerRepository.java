package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.customer.Customer;
import com.parctrack.domain.customer.CustomerRepository;
import com.parctrack.domain.equipment.AgreementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, UUID>, CustomerRepository {

    @Override
    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.organization.id = :organizationId AND c.deletedAt IS NULL")
    Optional<Customer> findByIdAndOrganizationId(@Param("id") UUID id, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :organizationId")
    Page<Customer> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Override
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :organizationId AND c.deletedAt IS NULL")
    Page<Customer> findByOrganizationIdAndDeletedAtIsNull(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Override
    @Query("SELECT c FROM Customer c WHERE c.organization.id = :organizationId AND c.deletedAt IS NULL ORDER BY c.name ASC")
    List<Customer> findByOrganizationIdAndDeletedAtIsNull(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT c FROM Customer c WHERE c.agreementStatus = :status AND c.contractEndDate < :date AND c.deletedAt IS NULL")
    List<Customer> findByAgreementStatusAndContractEndDateBefore(@Param("status") AgreementStatus status, @Param("date") LocalDate date);

    @Override
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.organization.id = :organizationId AND c.deletedAt IS NULL")
    long countByOrganizationId(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.name = :name AND c.organization.id = :organizationId AND c.deletedAt IS NULL")
    boolean existsByNameAndOrganizationId(@Param("name") String name, @Param("organizationId") UUID organizationId);
}
