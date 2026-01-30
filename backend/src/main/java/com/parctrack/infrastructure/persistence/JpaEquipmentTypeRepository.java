package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.equipment.EquipmentType;
import com.parctrack.domain.equipment.EquipmentTypeRepository;
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
public interface JpaEquipmentTypeRepository extends JpaRepository<EquipmentType, UUID>, EquipmentTypeRepository {

    @Override
    @Query("SELECT et FROM EquipmentType et WHERE et.id = :id AND et.organization.id = :organizationId")
    Optional<EquipmentType> findByIdAndOrganizationId(@Param("id") UUID id, @Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT et FROM EquipmentType et WHERE et.organization.id = :organizationId")
    Page<EquipmentType> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Override
    @Query("SELECT et FROM EquipmentType et WHERE et.organization.id = :organizationId ORDER BY et.displayOrder ASC")
    List<EquipmentType> findByOrganizationIdOrderByDisplayOrderAsc(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT et FROM EquipmentType et WHERE et.organization.id = :organizationId AND et.active = true")
    List<EquipmentType> findByOrganizationIdAndActiveTrue(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT et FROM EquipmentType et WHERE et.organization.id = :organizationId AND et.active = true ORDER BY et.displayOrder ASC")
    List<EquipmentType> findByOrganizationIdAndActiveTrueOrderByDisplayOrderAsc(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT COUNT(et) FROM EquipmentType et WHERE et.organization.id = :organizationId")
    long countByOrganizationId(@Param("organizationId") UUID organizationId);

    @Override
    @Query("SELECT CASE WHEN COUNT(et) > 0 THEN true ELSE false END FROM EquipmentType et WHERE et.name = :name AND et.organization.id = :organizationId")
    boolean existsByNameAndOrganizationId(@Param("name") String name, @Param("organizationId") UUID organizationId);
}
