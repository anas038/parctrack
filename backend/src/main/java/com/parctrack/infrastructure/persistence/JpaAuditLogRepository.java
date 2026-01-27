package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.audit.AuditLog;
import com.parctrack.domain.audit.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JpaAuditLogRepository extends JpaRepository<AuditLog, UUID>, AuditLogRepository {
    @Override
    Page<AuditLog> findByOrganizationId(UUID organizationId, Pageable pageable);
}
