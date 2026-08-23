package com.mystikos.payment.domain.repository;

import com.mystikos.payment.domain.model.Wallet;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository {

    Optional<Wallet> findByUserId(Long userId);

    /** 用户第一次涉及资金操作时按需建一个零余额钱包，已存在则直接返回。 */
    Wallet findOrCreate(Long userId, String currency);

    /** 原子加余额（充值到账/收到礼物）。 */
    void credit(Long walletId, BigDecimal amount);

    /**
     * 原子扣余额，SQL 层用 {@code WHERE balance >= amount} 保证不会扣成负数。
     * @return 扣款是否成功；false 表示余额不足，调用方不需要另外查一次余额判断。
     */
    boolean debit(Long walletId, BigDecimal amount);
}
