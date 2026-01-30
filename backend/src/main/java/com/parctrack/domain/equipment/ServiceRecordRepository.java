package com.parctrack.domain.equipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRecordRepository {
    ServiceRecord save(ServiceRecord serviceRecord);
    Optional<ServiceRecord> findById(UUID id);
    List<ServiceRecord> findByEquipmentId(UUID equipmentId);
    Page<ServiceRecord> findByEquipmentId(UUID equipmentId, Pageable pageable);
    List<ServiceRecord> findByEquipmentIdAndServicedAtAfter(UUID equipmentId, Instant since);
    List<ServiceRecord> findByEquipmentIdOrderByServicedAtDesc(UUID equipmentId);
    long countByEquipmentId(UUID equipmentId);
    long countByEquipmentIdAndServicedAtBetween(UUID equipmentId, Instant start, Instant end);
    void deleteById(UUID id);
    void deleteByEquipmentId(UUID equipmentId);
}
