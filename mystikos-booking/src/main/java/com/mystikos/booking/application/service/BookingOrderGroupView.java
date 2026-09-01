package com.mystikos.booking.application.service;

import com.mystikos.booking.domain.model.BookingGroupStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BookingOrderGroupView(Long id, BookingGroupStatus status, BigDecimal totalAmount,
                                     OffsetDateTime createdAt, OffsetDateTime expiresAt,
                                     List<BookingOrderView> bookings) {
}
