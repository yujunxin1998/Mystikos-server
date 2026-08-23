package com.mystikos.commerce.domain.event;

import com.mystikos.common.event.DomainEvent;

import java.math.BigDecimal;

public class OrderPlacedEvent extends DomainEvent {

    private final Long orderId;
    private final Long patronId;
    private final BigDecimal totalAmount;

    public OrderPlacedEvent(Long orderId, Long patronId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.patronId = patronId;
        this.totalAmount = totalAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getPatronId() {
        return patronId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
