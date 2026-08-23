package com.mystikos.commerce.infrastructure.acl;

import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.payment.domain.event.PaymentCapturedEvent;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付成功后把商城订单从 PENDING_PAYMENT 推进到 PAID。按 sourceType 过滤，
 * 不响应 Booking/Gifting/钱包充值那几路的 PaymentCaptured。
 *
 * <p>类名带 Commerce 前缀，理由见 {@link CommercePaymentPortImpl} 类注释。
 */
@Component
public class CommercePaymentCapturedEventListener {

    private final CommerceApplicationService commerceApplicationService;

    public CommercePaymentCapturedEventListener(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PaymentCapturedEvent event) {
        if (event.getSourceType() != SourceType.MERCHANDISE) {
            return;
        }
        commerceApplicationService.markPaid(event.getSourceId());
    }
}
