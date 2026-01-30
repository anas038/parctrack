package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.site.Site;
import com.parctrack.domain.site.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSiteRepository extends JpaRepository<Site, UUID>, SiteRepository {

    @Override
    @Query("SELECT s FROM Site s WHERE s.id = :id AND s.customer.organization.id = :organizationId AND s.deletedAt IS NULL")
    Optional<Site> findByIdAndCustomerOrganizationId(@Param("id") UUID id, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT s FROM Site s WHERE s.customer.id = :customerId")
    Page<Site> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    @Override
    @Query("SELECT s FROM Site s WHERE s.customer.id = :customerId AND s.deletedAt IS NULL")
    Page<Site> findByCustomerIdAndDeletedAtIsNull(@Param("customerId") UUID customerId, Pageable pageable);

    @Override
    @Query("SELECT s FROM Site s WHERE s.customer.id = :customerId AND s.deletedAt IS NULL ORDER BY s.name ASC")
    List<Site> findByCustomerIdAndDeletedAtIsNull(@Param("customerId") UUID customerId);

    @Override
    @Query("SELECT s FROM Site s WHERE s.customer.organization.id = :organizationId")
    Page<Site> findByCustomerOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Override
    @Query("SELECT s FROM Site s WHERE s.customer.organization.id = :organizationId AND s.deletedAt IS NULL")
    Page<Site> findByCustomerOrganizationIdAndDeletedAtIsNull(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Override
    @Query("SELECT s FROM Site s WHERE s.customer.organization.id = :organizationId AND s.deletedAt IS NULL ORDER BY s.customer.name ASC, s.name ASC")
    List<Site> findByCustomerOrganizationIdAndDeletedAtIsNull(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT COUNT(s) FROM Site s WHERE s.customer.id = :customerId AND s.deletedAt IS NULL")
    long countByCustomerId(@Param("customerId") UUID customerId);

    @Override
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Site s WHERE s.name = :name AND s.customer.id = :customerId AND s.deletedAt IS NULL")
    boolean existsByNameAndCustomerId(@Param("name") String name, @Param("customerId") UUID customerId);
}
