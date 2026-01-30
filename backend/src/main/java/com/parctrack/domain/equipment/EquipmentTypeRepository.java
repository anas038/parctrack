package com.parctrack.domain.equipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentTypeRepository {
    EquipmentType save(EquipmentType equipmentType);
    Optional<EquipmentType> findById(UUID id);
    Optional<EquipmentType> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Page<EquipmentType> findByOrganizationId(UUID organizationId, Pageable pageable);
    List<EquipmentType> findByOrganizationIdOrderByDisplayOrderAsc(UUID organizationId);
    List<EquipmentType> findByOrganizationIdAndActiveTrue(UUID organizationId);
    List<EquipmentType> findByOrganizationIdAndActiveTrueOrderByDisplayOrderAsc(UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
    void deleteById(UUID id);
}
