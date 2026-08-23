package com.mystikos.commerce.application.service;

import com.mystikos.commerce.domain.model.OrderLineItem;
import com.mystikos.commerce.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderView(Long orderId, Long patronId, List<OrderLineItem> items, BigDecimal totalAmount,
                         String shippingAddress, OrderStatus status, OffsetDateTime createdAt) {
}
