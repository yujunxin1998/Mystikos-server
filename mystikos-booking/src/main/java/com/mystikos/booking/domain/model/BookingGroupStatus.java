package com.mystikos.booking.domain.model;

/**
 * 预约组状态机，只管到 PAID 为止：DRAFT → PENDING_PAYMENT → PAID，旁路 EXPIRED/CANCELLED。
 * PAID 之后组不再参与——每个子预约（BookingOrder）各自走自己完整的 BookingStatus
 * （MATCHING/ACCEPTED/IN_SERVICE/COMPLETED，也可能各自 CANCELLED/DISPUTED/REFUNDED），
 * 见 BookingOrderGroup 类注释。
 */
public enum BookingGroupStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    EXPIRED,
    CANCELLED
}
