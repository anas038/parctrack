package com.parctrack.infrastructure.scheduling;

import com.parctrack.domain.equipment.Equipment;
import com.parctrack.domain.equipment.EquipmentRepository;
import com.parctrack.domain.equipment.ServiceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class ProvisionalCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(ProvisionalCleanupJob.class);

    private final EquipmentRepository equipmentRepository;
    private final ServiceRecordRepository serviceRecordRepository;

    public ProvisionalCleanupJob(
            EquipmentRepository equipmentRepository,
            ServiceRecordRepository serviceRecordRepository) {
        this.equipmentRepository = equipmentRepository;
        this.serviceRecordRepository = serviceRecordRepository;
    }

    @Scheduled(cron = "0 0 2 * * *") // Nightly at 2:00 AM
    @Transactional
    public void cleanupExpiredProvisionalEquipment() {
        logger.info("Starting provisional equipment cleanup");

        Instant now = Instant.now();

        // Find all provisional equipment that has expired
        List<Equipment> expiredProvisional = equipmentRepository
                .findByProvisionalTrueAndProvisionalExpiresAtBefore(now);

        int deletedCount = 0;
        for (Equipment equipment : expiredProvisional) {
            // Delete associated service records first
            serviceRecordRepository.deleteByEquipmentId(equipment.getId());

            // Hard delete the provisional equipment (they were never formalized)
            equipmentRepository.deleteById(equipment.getId());
            deletedCount++;
            logger.debug("Deleted expired provisional equipment {} (serial: {}, expired at: {})",
                    equipment.getId(), equipment.getSerialNumber(), equipment.getProvisionalExpiresAt());
        }

        logger.info("Provisional equipment cleanup complete. Deleted {} expired provisional records",
                deletedCount);
    }
}
