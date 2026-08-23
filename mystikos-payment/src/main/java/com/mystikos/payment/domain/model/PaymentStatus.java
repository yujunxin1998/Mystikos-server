package com.mystikos.payment.domain.model;

/**
 * CREATED → REQUIRES_ACTION → CAPTURED，旁路 FAILED / REFUNDED。
 * 内部钱包扣款（GIFT 来源）没有 REQUIRES_ACTION 阶段，创建即 CAPTURED。
 */
public enum PaymentStatus {
    CREATED,
    REQUIRES_ACTION,
    CAPTURED,
    FAILED,
    REFUNDED
}
