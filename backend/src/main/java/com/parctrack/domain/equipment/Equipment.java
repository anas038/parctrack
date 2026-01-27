package com.parctrack.domain.equipment;

import com.parctrack.domain.common.BaseEntity;
import com.parctrack.domain.organization.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
public class Equipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "cust_asset_id")
    private String custAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_status", nullable = false)
    private AgreementStatus agreementStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_cycle", nullable = false)
    private ServiceCycle serviceCycle;

    @Column(name = "last_service")
    private Instant lastService;

    @Column(name = "next_service")
    private LocalDate nextService;

    @Column(name = "next_service_override")
    private boolean nextServiceOverride = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Equipment() {
    }

    public Equipment(Organization organization, String serialNumber, AgreementStatus agreementStatus, ServiceCycle serviceCycle) {
        this.organization = organization;
        this.serialNumber = serialNumber;
        this.agreementStatus = agreementStatus;
        this.serviceCycle = serviceCycle;
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

    public String getQrCodeValue() {
        return serialNumber;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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

    public AgreementStatus getAgreementStatus() {
        return agreementStatus;
    }

    public void setAgreementStatus(AgreementStatus agreementStatus) {
        this.agreementStatus = agreementStatus;
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
