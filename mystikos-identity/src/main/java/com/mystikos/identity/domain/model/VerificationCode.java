package com.mystikos.identity.domain.model;

import com.mystikos.identity.domain.IdentityException;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * 一次性验证码。5 分钟有效期，一次消费后作废——不是给"记住验证码"用的缓存。
 */
public class VerificationCode {

    private Long id;
    private final AuthChannel channel;
    private final String identifier;
    private final VerificationPurpose purpose;
    private final String code;
    private final OffsetDateTime expiresAt;
    private OffsetDateTime consumedAt;

    private VerificationCode(Long id, AuthChannel channel, String identifier, VerificationPurpose purpose,
                              String code, OffsetDateTime expiresAt, OffsetDateTime consumedAt) {
        this.id = id;
        this.channel = channel;
        this.identifier = identifier;
        this.purpose = purpose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
    }

    public static VerificationCode issue(AuthChannel channel, String identifier, VerificationPurpose purpose,
                                          String code, Duration ttl) {
        return new VerificationCode(null, channel, identifier, purpose, code,
                OffsetDateTime.now().plus(ttl), null);
    }

    public static VerificationCode restore(Long id, AuthChannel channel, String identifier,
                                            VerificationPurpose purpose, String code,
                                            OffsetDateTime expiresAt, OffsetDateTime consumedAt) {
        return new VerificationCode(id, channel, identifier, purpose, code, expiresAt, consumedAt);
    }

    /** 校验并消费；失败/过期/已用过都抛业务异常，不返回 boolean 让调用方自己判断。 */
    public void consume(String inputCode) {
        if (consumedAt != null) {
            throw IdentityException.verificationCodeInvalid();
        }
        if (OffsetDateTime.now().isAfter(expiresAt)) {
            throw IdentityException.verificationCodeExpired();
        }
        if (!code.equals(inputCode)) {
            throw IdentityException.verificationCodeInvalid();
        }
        consumedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public AuthChannel getChannel() {
        return channel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public VerificationPurpose getPurpose() {
        return purpose;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }
}
