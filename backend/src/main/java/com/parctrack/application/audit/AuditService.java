package com.parctrack.application.audit;

import com.parctrack.domain.audit.AuditLog;
import com.parctrack.domain.audit.AuditLogRepository;
import com.parctrack.infrastructure.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String action) {
        AuditLog log = AuditLog.create(action)
                .withOrganization(TenantContext.getCurrentTenant())
                .withUser(TenantContext.getCurrentUserId())
                .withIpAddress(getClientIpAddress());
        auditLogRepository.save(log);
    }

    public void logAction(String action, String resourceType, UUID resourceId) {
        AuditLog log = AuditLog.create(action)
                .withOrganization(TenantContext.getCurrentTenant())
                .withUser(TenantContext.getCurrentUserId())
                .withResource(resourceType, resourceId)
                .withIpAddress(getClientIpAddress());
        auditLogRepository.save(log);
    }

    public void logAction(String action, String resourceType, UUID resourceId, String details) {
        AuditLog log = AuditLog.create(action)
                .withOrganization(TenantContext.getCurrentTenant())
                .withUser(TenantContext.getCurrentUserId())
                .withResource(resourceType, resourceId)
                .withDetails(details)
                .withIpAddress(getClientIpAddress());
        auditLogRepository.save(log);
    }

    public void logAuthEvent(String action, UUID userId, UUID organizationId, String details) {
        AuditLog log = AuditLog.create(action)
                .withOrganization(organizationId)
                .withUser(userId)
                .withDetails(details)
                .withIpAddress(getClientIpAddress());
        auditLogRepository.save(log);
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
