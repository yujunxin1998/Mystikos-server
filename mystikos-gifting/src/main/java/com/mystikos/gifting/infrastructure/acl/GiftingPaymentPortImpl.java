package com.mystikos.gifting.infrastructure.acl;

import com.mystikos.gifting.application.port.PaymentPort;
import com.mystikos.gifting.domain.GiftingException;
import com.mystikos.payment.application.service.WalletApplicationService;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.PaymentResponseCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 防腐层：把 mystikos-payment 的 WalletApplicationService 抛出的余额不足异常
 * 翻译成本模块自己的 GiftingException，不把 Payment 的异常类型透出到
 * GiftApplicationService 之外。
 *
 * <p>类名带 Gifting 前缀——全模块扁平扫描下不能和 Booking/Commerce 各自的
 * 同职责 PaymentPortImpl 简单类名重复，否则启动时报 ConflictingBeanDefinitionException。
 */
@Component
public class GiftingPaymentPortImpl implements PaymentPort {

    private final WalletApplicationService walletApplicationService;

    public GiftingPaymentPortImpl(WalletApplicationService walletApplicationService) {
        this.walletApplicationService = walletApplicationService;
    }

    @Override
    public void debitWallet(Long patronId, Long companionId, Long giftTransactionId, BigDecimal amount, String currency) {
        try {
            walletApplicationService.debitForGift(patronId, companionId, giftTransactionId, amount, currency);
        } catch (PaymentException e) {
            if (e.getResultCode() == PaymentResponseCode.INSUFFICIENT_BALANCE) {
                throw GiftingException.insufficientBalance();
            }
            throw e;
        }
    }

    @Override
    public void refundWallet(Long patronId, Long companionId, Long giftTransactionId, BigDecimal amount, String currency) {
        try {
            walletApplicationService.refundForGift(patronId, companionId, giftTransactionId, amount, currency);
        } catch (PaymentException e) {
            if (e.getResultCode() == PaymentResponseCode.INSUFFICIENT_BALANCE) {
                throw GiftingException.insufficientBalance();
            }
            throw e;
        }
    }
}
