package com.parctrack.application.dto.customer;

import com.parctrack.domain.customer.Customer;
import com.parctrack.domain.equipment.AgreementStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String name,
        AgreementStatus agreementStatus,
        LocalDate contractEndDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerDto from(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getAgreementStatus(),
                customer.getContractEndDate(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
