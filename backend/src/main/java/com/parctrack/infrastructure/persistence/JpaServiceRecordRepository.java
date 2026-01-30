package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.equipment.ServiceRecord;
import com.parctrack.domain.equipment.ServiceRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaServiceRecordRepository extends JpaRepository<ServiceRecord, UUID>, ServiceRecordRepository {

    @Override
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId")
    List<ServiceRecord> findByEquipmentId(@Param("equipmentId") UUID equipmentId);

    @Override
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId")
    Page<ServiceRecord> findByEquipmentId(@Param("equipmentId") UUID equipmentId, Pageable pageable);

    @Override
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId AND sr.servicedAt > :since ORDER BY sr.servicedAt DESC")
    List<ServiceRecord> findByEquipmentIdAndServicedAtAfter(@Param("equipmentId") UUID equipmentId, @Param("since") Instant since);

    @Override
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId ORDER BY sr.servicedAt DESC")
    List<ServiceRecord> findByEquipmentIdOrderByServicedAtDesc(@Param("equipmentId") UUID equipmentId);

    @Override
    @Query("SELECT COUNT(sr) FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId")
    long countByEquipmentId(@Param("equipmentId") UUID equipmentId);

    @Override
    @Query("SELECT COUNT(sr) FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId AND sr.servicedAt BETWEEN :start AND :end")
    long countByEquipmentIdAndServicedAtBetween(@Param("equipmentId") UUID equipmentId, @Param("start") Instant start, @Param("end") Instant end);

    @Override
    @Modifying
    @Query("DELETE FROM ServiceRecord sr WHERE sr.equipment.id = :equipmentId")
    void deleteByEquipmentId(@Param("equipmentId") UUID equipmentId);
}
