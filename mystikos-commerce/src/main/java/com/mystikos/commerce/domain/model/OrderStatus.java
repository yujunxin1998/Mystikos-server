package com.mystikos.commerce.domain.model;

/**
 * DRAFT → PENDING_PAYMENT → PAID → FULFILLING → SHIPPED → COMPLETED，
 * 旁路 CANCELLED / REFUNDED（见 docs/architecture/domain-model.md）。
 * C 端只接了 create/cancel 两个用例；FULFILLING/SHIPPED/COMPLETED/REFUNDED 的流转
 * 由后台管理接口驱动，见 {@link com.mystikos.commerce.adapter.web.OrderAdminController}。
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
