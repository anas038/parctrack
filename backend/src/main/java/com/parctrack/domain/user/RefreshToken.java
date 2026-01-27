package com.parctrack.domain.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "extended_session")
    private boolean extendedSession = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RefreshToken() {
        this.createdAt = Instant.now();
    }

    public RefreshToken(User user, String token, Instant expiresAt, boolean extendedSession) {
        this();
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.extendedSession = extendedSession;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExtendedSession() {
        return extendedSession;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
