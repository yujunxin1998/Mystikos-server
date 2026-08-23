package com.mystikos.payment.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 不可变账本行，append-only——一旦写入不会被修改或删除，任何资金移动的更正
 * 都应该是新增一条反向记录，不是改这一行。照 GiftTransaction 的"只有 create+restore，
 * 没有 update"模式。
 */
public final class LedgerEntry {

    private Long id;
    private final Long intentId;
    private final Long walletId;
    private final LedgerDirection direction;
    private final BigDecimal amount;
    private final String currency;
    private final OffsetDateTime occurredAt;

    private LedgerEntry(Long id, Long intentId, Long walletId, LedgerDirection direction,
                         BigDecimal amount, String currency, OffsetDateTime occurredAt) {
        this.id = id;
        this.intentId = intentId;
        this.walletId = walletId;
        this.direction = direction;
        this.amount = amount;
        this.currency = currency;
        this.occurredAt = occurredAt;
    }

    /** 记一笔账。walletId 为空表示这笔资金移动发生在平台和外部网关之间，不涉及内部钱包。 */
    public static LedgerEntry record(Long intentId, Long walletId, LedgerDirection direction,
                                      BigDecimal amount, String currency) {
        return new LedgerEntry(null, intentId, walletId, direction, amount, currency, OffsetDateTime.now());
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static LedgerEntry restore(Long id, Long intentId, Long walletId, LedgerDirection direction,
                                       BigDecimal amount, String currency, OffsetDateTime occurredAt) {
        return new LedgerEntry(id, intentId, walletId, direction, amount, currency, occurredAt);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getIntentId() {
        return intentId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public LedgerDirection getDirection() {
        return direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
