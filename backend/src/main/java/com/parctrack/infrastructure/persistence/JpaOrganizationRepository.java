package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.organization.OrganizationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOrganizationRepository extends JpaRepository<Organization, UUID>, OrganizationRepository {
    @Override
    Optional<Organization> findByName(String name);
}
