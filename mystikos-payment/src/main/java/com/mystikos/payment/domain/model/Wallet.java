package com.mystikos.payment.domain.model;

import java.math.BigDecimal;

/**
 * 用户在平台内的记账余额——不是托管客户资金的持牌电子钱包。真实资金全程留在
 * Stripe 的平台余额里，这张表只是"这个用户在我们这边还能花多少/挣了多少"的账本。
 *
 * <p>余额变更不在这个聚合里做读-改-写（并发下不安全，两个并发请求都读到旧值
 * 会互相覆盖），而是由 {@link com.mystikos.payment.domain.repository.WalletRepository}
 * 用原子 UPDATE 语句直接在数据库层完成；这个类只是操作结果的只读快照。
 */
public final class Wallet {

    private final Long id;
    private final Long userId;
    private final BigDecimal balance;
    private final String currency;

    private Wallet(Long id, Long userId, BigDecimal balance, String currency) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.currency = currency;
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static Wallet restore(Long id, Long userId, BigDecimal balance, String currency) {
        return new Wallet(id, userId, balance, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
