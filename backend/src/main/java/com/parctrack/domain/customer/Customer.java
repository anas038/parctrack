package com.parctrack.domain.customer;

import com.parctrack.domain.common.BaseEntity;
import com.parctrack.domain.equipment.AgreementStatus;
import com.parctrack.domain.organization.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_status", nullable = false)
    private AgreementStatus agreementStatus;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Customer() {
    }

    public Customer(Organization organization, String name, AgreementStatus agreementStatus) {
        this.organization = organization;
        this.name = name;
        this.agreementStatus = agreementStatus;
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

    public boolean isContractExpired() {
        return contractEndDate != null && contractEndDate.isBefore(LocalDate.now());
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AgreementStatus getAgreementStatus() {
        return agreementStatus;
    }

    public void setAgreementStatus(AgreementStatus agreementStatus) {
        this.agreementStatus = agreementStatus;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
