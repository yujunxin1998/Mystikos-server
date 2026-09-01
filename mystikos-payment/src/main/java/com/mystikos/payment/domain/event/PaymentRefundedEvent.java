package com.mystikos.payment.domain.event;

import com.mystikos.common.event.DomainEvent;
import com.mystikos.payment.domain.model.SourceType;

import java.math.BigDecimal;

public class PaymentRefundedEvent extends DomainEvent {

    private final Long intentId;
    private final SourceType sourceType;
    private final Long sourceId;
    private final Long patronId;
    private final BigDecimal amount;
    private final String currency;

    public PaymentRefundedEvent(Long intentId, SourceType sourceType, Long sourceId, Long patronId,
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
