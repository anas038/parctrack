package com.parctrack.domain.equipment;

import com.parctrack.domain.customer.Customer;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class StoplightService {

    private static final int WARNING_DAYS_THRESHOLD = 15;

    public StoplightStatus calculateStatus(Equipment equipment) {
        if (equipment.isDeleted()) {
            return StoplightStatus.RED;
        }

        // Get agreement status from customer if site exists, otherwise from equipment
        AgreementStatus agreementStatus = getEffectiveAgreementStatus(equipment);
        LocalDate nextService = equipment.getNextService();
        LocalDate today = LocalDate.now();

        // Red: Out of Scope OR Pending Agreement OR overdue
        if (agreementStatus == AgreementStatus.OUT_OF_SCOPE) {
            return StoplightStatus.RED;
        }
        if (agreementStatus == AgreementStatus.PENDING) {
            return StoplightStatus.RED;
        }
        if (nextService != null && nextService.isBefore(today)) {
            return StoplightStatus.RED;
        }

        // Yellow: Within warning threshold
        if (nextService != null) {
            LocalDate warningDate = today.plusDays(WARNING_DAYS_THRESHOLD);
            if (!nextService.isAfter(warningDate)) {
                return StoplightStatus.YELLOW;
            }
        }

        // Green: Covered AND > 15 days until next_service
        return StoplightStatus.GREEN;
    }

    public AgreementStatus getEffectiveAgreementStatus(Equipment equipment) {
        // Per spec: agreement_status is now on Customer, not Equipment
        // Use customer's agreement status if site exists
        if (equipment.getSite() != null) {
            Customer customer = equipment.getSite().getCustomer();
            if (customer != null) {
                return customer.getAgreementStatus();
            }
        }
        // Fallback to equipment's own agreement status for backward compatibility
        return equipment.getAgreementStatus();
    }

    public boolean isOverdue(Equipment equipment) {
        LocalDate nextService = equipment.getNextService();
        return nextService != null && nextService.isBefore(LocalDate.now());
    }

    public boolean isWarning(Equipment equipment) {
        return calculateStatus(equipment) == StoplightStatus.YELLOW;
    }

    public boolean isRed(Equipment equipment) {
        return calculateStatus(equipment) == StoplightStatus.RED;
    }
}
