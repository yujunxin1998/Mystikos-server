package com.mystikos.commerce.domain.model;

/**
 * DRAFT → PENDING_PAYMENT → PAID → FULFILLING → SHIPPED → COMPLETED，
 * 旁路 CANCELLED / REFUNDED（见 docs/architecture/domain-model.md）。
 * 当前只有 create/cancel 两个用例接了应用层，其余流转方法先留在聚合上，
 * 和 mystikos-booking 的 BookingOrder 是同一个"打样但暂不全接"的做法。
 */
public enum OrderStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    FULFILLING,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}
