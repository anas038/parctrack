package com.parctrack.domain.equipment;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class StoplightService {

    private static final int WARNING_DAYS_THRESHOLD = 15;

    public StoplightStatus calculateStatus(Equipment equipment) {
        if (equipment.isDeleted()) {
            return StoplightStatus.RED;
        }

        AgreementStatus agreementStatus = equipment.getAgreementStatus();
        LocalDate nextService = equipment.getNextService();
        LocalDate today = LocalDate.now();

        // Red: Out of Scope OR overdue
        if (agreementStatus == AgreementStatus.OUT_OF_SCOPE) {
            return StoplightStatus.RED;
        }
        if (nextService != null && nextService.isBefore(today)) {
            return StoplightStatus.RED;
        }

        // Yellow: Pending agreement OR within warning threshold
        if (agreementStatus == AgreementStatus.PENDING) {
            return StoplightStatus.YELLOW;
        }
        if (nextService != null) {
            LocalDate warningDate = today.plusDays(WARNING_DAYS_THRESHOLD);
            if (!nextService.isAfter(warningDate)) {
                return StoplightStatus.YELLOW;
            }
        }

        // Green: Covered AND > 15 days until next_service
        return StoplightStatus.GREEN;
    }

    public boolean isOverdue(Equipment equipment) {
        LocalDate nextService = equipment.getNextService();
        return nextService != null && nextService.isBefore(LocalDate.now());
    }

    public boolean isWarning(Equipment equipment) {
        return calculateStatus(equipment) == StoplightStatus.YELLOW;
    }
}
