package com.parctrack.domain.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
    Page<AuditLog> findByOrganizationId(UUID organizationId, Pageable pageable);
}
