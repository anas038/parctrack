package com.parctrack.application.dto.customer;

import com.parctrack.domain.equipment.AgreementStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotNull(message = "Agreement status is required")
        AgreementStatus agreementStatus,
        LocalDate contractEndDate
) {}
