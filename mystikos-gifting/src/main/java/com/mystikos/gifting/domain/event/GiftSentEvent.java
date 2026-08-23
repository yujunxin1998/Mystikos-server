package com.mystikos.gifting.domain.event;

import com.mystikos.common.event.DomainEvent;

import java.math.BigDecimal;

/**
 * 赠礼完成事件。下游消费方：Relationship（累加亲密度进度）、Leaderboard（累加魅力值/守护值）、
 * Membership（当前先顶替 PaymentCaptured 做消费累计，见 docs/architecture/domain-model.md）。
 */
public class GiftSentEvent extends DomainEvent {

    private final Long giftTransactionId;
    private final Long patronId;
    private final Long companionId;
    private final Long giftId;
    private final int quantity;
    private final BigDecimal amount;

    public GiftSentEvent(Long giftTransactionId, Long patronId, Long companionId,
                          Long giftId, int quantity, BigDecimal amount) {
        this.giftTransactionId = giftTransactionId;
        this.patronId = patronId;
        this.companionId = companionId;
        this.giftId = giftId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public Long getGiftTransactionId() {
        return giftTransactionId;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public Long getGiftId() {
        return giftId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
