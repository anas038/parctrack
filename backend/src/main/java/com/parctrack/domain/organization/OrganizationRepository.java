package com.parctrack.domain.organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(UUID id);
    Optional<Organization> findByName(String name);
    boolean existsById(UUID id);
}
