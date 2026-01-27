package com.parctrack.domain.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AuditLog() {
        this.createdAt = Instant.now();
    }

    public static AuditLog create(String action) {
        AuditLog log = new AuditLog();
        log.action = action;
        return log;
    }

    public AuditLog withOrganization(UUID organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public AuditLog withUser(UUID userId) {
        this.userId = userId;
        return this;
    }

    public AuditLog withResource(String resourceType, UUID resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        return this;
    }

    public AuditLog withDetails(String details) {
        this.details = details;
        return this;
    }

    public AuditLog withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getDetails() {
        return details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
