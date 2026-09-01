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

    /**
     * 退款：反向操作——退老板余额、从陪玩余额扣回。陪玩余额不足（已提现/已花掉）会抛
     * {@link com.mystikos.gifting.domain.GiftingException}，调用方（GiftApplicationService）
     * 让整个退款事务回滚，这笔赠礼流水维持 COMPLETED，不会出现"退了钱但流水状态没变"。
     */
    void refundWallet(Long patronId, Long companionId, Long giftTransactionId, BigDecimal amount, String currency);
}
