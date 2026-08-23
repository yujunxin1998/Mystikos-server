package com.mystikos.payment.domain.event;

import com.mystikos.common.event.DomainEvent;
import com.mystikos.payment.domain.model.SourceType;

import java.math.BigDecimal;

/**
 * 支付成功入账。下游 Membership 订阅它累计消费（覆盖 Booking/Commerce/Gifting/
 * Wallet 充值四个来源），见 docs/architecture/domain-model.md 的上下文映射。
 */
public class PaymentCapturedEvent extends DomainEvent {

    private final Long intentId;
    private final SourceType sourceType;
    private final Long sourceId;
    private final Long patronId;
    private final BigDecimal amount;
    private final String currency;

    public PaymentCapturedEvent(Long intentId, SourceType sourceType, Long sourceId, Long patronId,
                                 BigDecimal amount, String currency) {
        this.intentId = intentId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.patronId = patronId;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getIntentId() {
        return intentId;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public Long getPatronId() {
        return patronId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
