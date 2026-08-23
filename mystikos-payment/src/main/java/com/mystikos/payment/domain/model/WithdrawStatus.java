package com.mystikos.payment.domain.model;

/** PENDING_REVIEW → APPROVED → PAID，旁路 REJECTED。打款只在 APPROVED 之后发生，禁止跳过人工审核直接打款。 */
public enum WithdrawStatus {
    PENDING_REVIEW,
    APPROVED,
    PAID,
    REJECTED
}
