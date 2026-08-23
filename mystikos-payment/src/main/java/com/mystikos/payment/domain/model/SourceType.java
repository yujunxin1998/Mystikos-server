package com.mystikos.payment.domain.model;

/**
 * PaymentIntent 回指的业务来源。Payment 只用 sourceType + sourceId 回指，不持有业务细节，
 * 见 docs/architecture/domain-model.md 的 Payment & Ledger 小节。
 */
public enum SourceType {
    BOOKING,
    MERCHANDISE,
    GIFT,
    WALLET_RECHARGE
}
