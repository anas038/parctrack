package com.parctrack.domain.user;

import com.parctrack.domain.common.BaseEntity;
import com.parctrack.domain.organization.Organization;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "magic_link_token")
    private String magicLinkToken;

    @Column(name = "magic_link_expires_at")
    private Instant magicLinkExpiresAt;

    public User() {
    }

    public User(Organization organization, String email, String username, Role role) {
        this.organization = organization;
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void lock(Instant until) {
        this.lockedUntil = until;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getMagicLinkToken() {
        return magicLinkToken;
    }

    public void setMagicLinkToken(String magicLinkToken) {
        this.magicLinkToken = magicLinkToken;
    }

    public Instant getMagicLinkExpiresAt() {
        return magicLinkExpiresAt;
    }

    public void setMagicLinkExpiresAt(Instant magicLinkExpiresAt) {
        this.magicLinkExpiresAt = magicLinkExpiresAt;
    }
}
