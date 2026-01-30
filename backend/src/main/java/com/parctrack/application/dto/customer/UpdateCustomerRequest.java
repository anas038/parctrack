package com.parctrack.application.dto.customer;

import com.parctrack.domain.equipment.AgreementStatus;
import java.time.LocalDate;

public record UpdateCustomerRequest(
        String name,
        AgreementStatus agreementStatus,
        LocalDate contractEndDate
) {}
