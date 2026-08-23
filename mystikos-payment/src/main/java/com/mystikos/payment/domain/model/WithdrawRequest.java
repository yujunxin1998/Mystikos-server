package com.mystikos.payment.domain.model;

import com.mystikos.payment.domain.PaymentException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 陪玩提现申请。PENDING_REVIEW → APPROVED → PAID，旁路 REJECTED。打款（调用
 * Stripe Connect Transfer）只发生在 APPROVED 之后，禁止跳过人工审核直接打款——
 * 这是给"平台代收代付"这类高风险资金操作留的合规闸门，不是可选步骤。
 */
public class WithdrawRequest {

    private Long id;
    private final Long companionId;
    private final BigDecimal amount;
    private final String currency;
    private WithdrawStatus status;
    private String stripeTransferRef;
    private Long decidedBy;
    private OffsetDateTime decidedAt;
    private String rejectReason;
    private final OffsetDateTime requestedAt;

    private WithdrawRequest(Long id, Long companionId, BigDecimal amount, String currency, WithdrawStatus status,
                             String stripeTransferRef, Long decidedBy, OffsetDateTime decidedAt,
                             String rejectReason, OffsetDateTime requestedAt) {
        this.id = id;
        this.companionId = companionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.stripeTransferRef = stripeTransferRef;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
        this.rejectReason = rejectReason;
        this.requestedAt = requestedAt;
    }

    public static WithdrawRequest create(Long companionId, BigDecimal amount, String currency) {
        return new WithdrawRequest(null, companionId, amount, currency, WithdrawStatus.PENDING_REVIEW,
                null, null, null, null, OffsetDateTime.now());
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static WithdrawRequest restore(Long id, Long companionId, BigDecimal amount, String currency,
                                           WithdrawStatus status, String stripeTransferRef, Long decidedBy,
                                           OffsetDateTime decidedAt, String rejectReason, OffsetDateTime requestedAt) {
        return new WithdrawRequest(id, companionId, amount, currency, status, stripeTransferRef,
                decidedBy, decidedAt, rejectReason, requestedAt);
    }

    public void approve(Long reviewerId) {
        transition(WithdrawStatus.PENDING_REVIEW, WithdrawStatus.APPROVED);
        this.decidedBy = reviewerId;
        this.decidedAt = OffsetDateTime.now();
    }

    public void reject(Long reviewerId, String reason) {
        transition(WithdrawStatus.PENDING_REVIEW, WithdrawStatus.REJECTED);
        this.decidedBy = reviewerId;
        this.decidedAt = OffsetDateTime.now();
        this.rejectReason = reason;
    }

    public void markPaid(String stripeTransferRef) {
        transition(WithdrawStatus.APPROVED, WithdrawStatus.PAID);
        this.stripeTransferRef = stripeTransferRef;
    }

    private void transition(WithdrawStatus expected, WithdrawStatus next) {
        if (status != expected) {
            throw PaymentException.statusInvalid("期望状态 " + expected + "，实际状态 " + status);
        }
        status = next;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public WithdrawStatus getStatus() {
        return status;
    }

    public String getStripeTransferRef() {
        return stripeTransferRef;
    }

    public Long getDecidedBy() {
        return decidedBy;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }
}
