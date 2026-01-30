package com.parctrack.domain.equipment;

import com.parctrack.domain.common.BaseEntity;
import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.site.Site;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "equipment")
public class Equipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_id")
    private EquipmentType equipmentType;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "cust_asset_id")
    private String custAssetId;

    @Column(name = "qr_code_value")
    private String qrCodeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_status", nullable = false)
    private AgreementStatus agreementStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false)
    private LifecycleStatus lifecycleStatus = LifecycleStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_cycle", nullable = false)
    private ServiceCycle serviceCycle;

    @Column(name = "last_service")
    private Instant lastService;

    @Column(name = "next_service")
    private LocalDate nextService;

    @Column(name = "next_service_override")
    private boolean nextServiceOverride = false;

    @Column(name = "is_provisional", nullable = false)
    private boolean provisional = false;

    @Column(name = "provisional_expires_at")
    private Instant provisionalExpiresAt;

    @Column(name = "predecessor_id")
    private UUID predecessorId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Equipment() {
    }

    public Equipment(Organization organization, String serialNumber, AgreementStatus agreementStatus, ServiceCycle serviceCycle) {
        this.organization = organization;
        this.serialNumber = serialNumber;
        this.agreementStatus = agreementStatus;
        this.serviceCycle = serviceCycle;
        this.lifecycleStatus = LifecycleStatus.ACTIVE;
    }

    public Equipment(Site site, String serialNumber, ServiceCycle serviceCycle) {
        this.site = site;
        this.serialNumber = serialNumber;
        this.serviceCycle = serviceCycle;
        this.lifecycleStatus = LifecycleStatus.ACTIVE;
        this.agreementStatus = AgreementStatus.COVERED;
    }

    public void markServiced() {
        this.lastService = Instant.now();
        if (!nextServiceOverride) {
            this.nextService = LocalDate.now().plus(serviceCycle.getPeriod());
        }
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOrphaned() {
        return site == null || site.isDeleted() || site.getCustomer().isDeleted();
    }

    public boolean isProvisionalExpired() {
        return provisional && provisionalExpiresAt != null && provisionalExpiresAt.isBefore(Instant.now());
    }

    public Organization getOrganization() {
        if (organization != null) {
            return organization;
        }
        if (site != null && site.getCustomer() != null) {
            return site.getCustomer().getOrganization();
        }
        return null;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCustAssetId() {
        return custAssetId;
    }

    public void setCustAssetId(String custAssetId) {
        this.custAssetId = custAssetId;
    }

    public String getQrCodeValue() {
        return qrCodeValue != null ? qrCodeValue : serialNumber;
    }

    public void setQrCodeValue(String qrCodeValue) {
        this.qrCodeValue = qrCodeValue;
    }

    public AgreementStatus getAgreementStatus() {
        return agreementStatus;
    }

    public void setAgreementStatus(AgreementStatus agreementStatus) {
        this.agreementStatus = agreementStatus;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public ServiceCycle getServiceCycle() {
        return serviceCycle;
    }

    public void setServiceCycle(ServiceCycle serviceCycle) {
        this.serviceCycle = serviceCycle;
    }

    public Instant getLastService() {
        return lastService;
    }

    public void setLastService(Instant lastService) {
        this.lastService = lastService;
    }

    public LocalDate getNextService() {
        return nextService;
    }

    public void setNextService(LocalDate nextService) {
        this.nextService = nextService;
    }

    public boolean isNextServiceOverride() {
        return nextServiceOverride;
    }

    public void setNextServiceOverride(boolean nextServiceOverride) {
        this.nextServiceOverride = nextServiceOverride;
    }

    public boolean isProvisional() {
        return provisional;
    }

    public void setProvisional(boolean provisional) {
        this.provisional = provisional;
    }

    public Instant getProvisionalExpiresAt() {
        return provisionalExpiresAt;
    }

    public void setProvisionalExpiresAt(Instant provisionalExpiresAt) {
        this.provisionalExpiresAt = provisionalExpiresAt;
    }

    public UUID getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(UUID predecessorId) {
        this.predecessorId = predecessorId;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
