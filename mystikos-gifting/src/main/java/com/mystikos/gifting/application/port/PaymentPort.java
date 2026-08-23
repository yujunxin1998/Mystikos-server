package com.mystikos.gifting.application.port;

import java.math.BigDecimal;

/**
 * 出站端口：礼物打赏走钱包余额同步扣款（不是网关下单）。MVP 阶段本地注入
 * mystikos-payment 的 WalletApplicationService，拆微服务时换 Feign/HTTP 客户端。
 */
public interface PaymentPort {

    /**
     * 扣老板余额、全额转给陪玩余额。余额不足抛
     * {@link com.mystikos.gifting.domain.GiftingException}，调用方不会产生 GiftTransaction
     * ——前提是这个方法和创建 GiftTransaction 在同一个 @Transactional 边界内。
     */
    void debitWallet(Long patronId, Long companionId, Long giftTransactionId, BigDecimal amount, String currency);
}
