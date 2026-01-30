package com.parctrack.domain.equipment;

import com.parctrack.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_records")
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "serviced_at", nullable = false)
    private Instant servicedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviced_by_user_id", nullable = false)
    private User servicedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code")
    private ReasonCode reasonCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (servicedAt == null) {
            servicedAt = Instant.now();
        }
    }

    public ServiceRecord() {
    }

    public ServiceRecord(Equipment equipment, User servicedBy) {
        this.equipment = equipment;
        this.servicedBy = servicedBy;
        this.servicedAt = Instant.now();
    }

    public ServiceRecord(Equipment equipment, User servicedBy, ReasonCode reasonCode) {
        this.equipment = equipment;
        this.servicedBy = servicedBy;
        this.reasonCode = reasonCode;
        this.servicedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Instant getServicedAt() {
        return servicedAt;
    }

    public void setServicedAt(Instant servicedAt) {
        this.servicedAt = servicedAt;
    }

    public User getServicedBy() {
        return servicedBy;
    }

    public void setServicedBy(User servicedBy) {
        this.servicedBy = servicedBy;
    }

    public ReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(ReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
