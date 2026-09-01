package com.mystikos.gifting.domain.event;

import com.mystikos.common.event.DomainEvent;

import java.math.BigDecimal;

/**
 * 赠礼退款事件。唯一订阅方是 Relationship——用 {@code intimacyValue} 扣减亲密度累计值
 * （复用 Relationship 已经依赖 Gifting 的这条边，不需要新开依赖）。VIP 累计消费的扣减
 * 走 Membership 订阅 Payment 的 PaymentRefundedEvent（sourceType=GIFT），不经过这个事件
 * ——两个下游各走各自已有的依赖边，见架构文档"退款"一节。
 */
public class GiftRefundedEvent extends DomainEvent {

    private final Long giftTransactionId;
    private final Long patronId;
    private final Long companionId;
    private final BigDecimal amount;
    private final BigDecimal intimacyValue;

    public GiftRefundedEvent(Long giftTransactionId, Long patronId, Long companionId,
                              BigDecimal amount, BigDecimal intimacyValue) {
        this.giftTransactionId = giftTransactionId;
        this.patronId = patronId;
        this.companionId = companionId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getIntimacyValue() {
        return intimacyValue;
    }
}
