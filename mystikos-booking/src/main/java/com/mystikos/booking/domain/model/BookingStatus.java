package com.mystikos.booking.domain.model;

/**
 * DRAFT → PENDING_PAYMENT → PAID → MATCHING → ACCEPTED → IN_SERVICE → COMPLETED，
 * 旁路 CANCELLED / EXPIRED / DISPUTED / REFUNDED（见 docs/architecture/domain-model.md）。
 */
public enum BookingStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    MATCHING,
    ACCEPTED,
    IN_SERVICE,
    COMPLETED,
    CANCELLED,
    EXPIRED,
    DISPUTED,
    REFUNDED
}
