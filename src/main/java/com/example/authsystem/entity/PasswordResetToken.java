package com.example.authsystem.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;

    protected PasswordResetToken() { }

    public PasswordResetToken(User user, String tokenHash, Instant now) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.createdAt = now;
        this.expiresAt = now.plusSeconds(15 * 60);
    }

    public User getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isValid(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }
    public void markUsed(Instant now) { usedAt = now; }
}
