package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.application.service.WalletApplicationService;
import com.mystikos.payment.domain.event.PaymentCapturedEvent;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 钱包充值对应的 PaymentIntent 被 webhook 标记 CAPTURED 后，把钱记入用户余额。
 * 只监听自己模块发的事件，按 sourceType 过滤，不影响 Booking/Commerce/Gifting 那几路。
 */
@Component
public class WalletRechargeCapturedListener {

    private final WalletApplicationService walletApplicationService;

    public WalletRechargeCapturedListener(WalletApplicationService walletApplicationService) {
        this.walletApplicationService = walletApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PaymentCapturedEvent event) {
        if (event.getSourceType() != SourceType.WALLET_RECHARGE) {
            return;
        }
        walletApplicationService.onRechargeCaptured(event.getPatronId(), event.getAmount(), event.getCurrency());
    }
}
