package com.parctrack.application.dto.site;

import java.util.Map;
import java.util.UUID;

public record UpdateSiteRequest(
        UUID customerId,
        String name,
        String address,
        String contactName,
        String contactPhone,
        Map<String, Object> metadata
) {}
