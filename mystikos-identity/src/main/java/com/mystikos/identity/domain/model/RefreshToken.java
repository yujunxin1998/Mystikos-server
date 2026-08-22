package com.mystikos.identity.domain.model;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Refresh Token 是持久化的不透明随机串（存的是哈希），不是 JWT——
 * 这样才能真正做到"登出/换设备可主动吊销"，纯无状态 JWT 做不到这点。
 */
public class RefreshToken {

    private Long id;
    private final Long userId;
    private final String tokenHash;
    private final OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;

    private RefreshToken(Long id, Long userId, String tokenHash, OffsetDateTime expiresAt,
                          OffsetDateTime revokedAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public static RefreshToken issue(Long userId, String tokenHash, Duration ttl) {
        return new RefreshToken(null, userId, tokenHash, OffsetDateTime.now().plus(ttl), null);
    }

    public static RefreshToken restore(Long id, Long userId, String tokenHash, OffsetDateTime expiresAt,
                                        OffsetDateTime revokedAt) {
        return new RefreshToken(id, userId, tokenHash, expiresAt, revokedAt);
    }

    public boolean isValid() {
        return revokedAt == null && OffsetDateTime.now().isBefore(expiresAt);
    }

    public void revoke() {
        revokedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }
}
