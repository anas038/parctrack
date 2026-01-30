package com.parctrack.application.dto.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record CreateSiteRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,
        @NotBlank(message = "Name is required")
        String name,
        String address,
        String contactName,
        String contactPhone,
        Map<String, Object> metadata
) {}
