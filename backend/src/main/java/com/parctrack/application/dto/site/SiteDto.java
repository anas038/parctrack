package com.parctrack.application.dto.site;

import com.parctrack.domain.site.Site;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SiteDto(
        UUID id,
        UUID customerId,
        String customerName,
        String name,
        String address,
        String contactName,
        String contactPhone,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt
) {
    public static SiteDto from(Site site) {
        return new SiteDto(
                site.getId(),
                site.getCustomer().getId(),
                site.getCustomer().getName(),
                site.getName(),
                site.getAddress(),
                site.getContactName(),
                site.getContactPhone(),
                site.getMetadata(),
                site.getCreatedAt(),
                site.getUpdatedAt()
        );
    }
}
