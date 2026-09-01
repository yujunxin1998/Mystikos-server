package com.mystikos.gifting.domain.event;

import com.mystikos.common.event.DomainEvent;

import java.math.BigDecimal;

/**
 * 赠礼完成事件。下游消费方：Relationship（累加 intimacyValue 到亲密度进度）、
 * Leaderboard（累加 amount 到魅力值/守护值——不受档位倍率影响）。Membership 改为订阅
 * Payment 的 PaymentCapturedEvent（sourceType=GIFT），不再订阅这个事件。
 *
 * <p>{@code amount} 和 {@code intimacyValue} 是两个不同的数字：前者是原价，驱动钱包/VIP/
 * 排行榜；后者是原价 x 档位倍率，只驱动亲密度——秘典的规则是"档位倍率只影响亲密度"。
 */
public class GiftSentEvent extends DomainEvent {

    private final Long giftTransactionId;
    private final Long patronId;
    private final Long companionId;
    private final Long giftId;
    private final int quantity;
    private final BigDecimal amount;
    private final BigDecimal intimacyValue;

    public GiftSentEvent(Long giftTransactionId, Long patronId, Long companionId,
                          Long giftId, int quantity, BigDecimal amount, BigDecimal intimacyValue) {
        this.giftTransactionId = giftTransactionId;
        this.patronId = patronId;
        this.companionId = companionId;
        this.giftId = giftId;
        this.quantity = quantity;
        this.amount = amount;
        this.intimacyValue = intimacyValue;
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

    public BigDecimal getIntimacyValue() {
        return intimacyValue;
    }
}
