package com.mystikos.booking.application.service;

import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 订单详情/列表视图；expiresAt 只在 DRAFT/PENDING_PAYMENT 下有意义，供前端倒计时。 */
public record BookingOrderView(
        Long id,
        Long companionId,
        OffsetDateTime start,
        OffsetDateTime end,
        BigDecimal durationHours,
        BigDecimal priceSnapshot,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {

    public static BookingOrderView from(BookingOrder order) {
        return new BookingOrderView(
                order.getId(),
                order.getCompanionId(),
                order.getTimeRange().start(),
                order.getTimeRange().end(),
                order.getDurationHours(),
                order.getPriceSnapshot(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getExpiresAt());
    }
}
