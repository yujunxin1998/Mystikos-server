package com.mystikos.booking.infrastructure.acl;

import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.payment.domain.event.PaymentCapturedEvent;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付成功后把预约订单从 PENDING_PAYMENT 推进到 PAID。按 sourceType 过滤，
 * 不响应 Commerce/Gifting/钱包充值那几路的 PaymentCaptured。
 *
 * <p>类名带 Booking 前缀，理由见 {@link BookingPaymentPortImpl} 类注释——
 * 全模块扁平扫描下简单类名不能和 Commerce/Membership 各自的同职责监听器重名。
 */
@Component
public class BookingPaymentCapturedEventListener {

    private final BookingApplicationService bookingApplicationService;

    public BookingPaymentCapturedEventListener(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PaymentCapturedEvent event) {
        if (event.getSourceType() != SourceType.BOOKING) {
            return;
        }
        bookingApplicationService.markPaid(event.getSourceId());
    }
}
