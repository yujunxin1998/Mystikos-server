package com.mystikos.booking.application.service;

import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.application.port.PaymentCheckoutResult;
import com.mystikos.booking.application.port.PaymentPort;
import com.mystikos.booking.domain.BookingException;
import com.mystikos.booking.domain.event.BookingCreatedEvent;
import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingRepository;
import com.mystikos.common.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预约撮合用例编排。跨限界上下文的调用（校验陪玩定价/档期、发起支付）
 * 应经 application/port 接口对接 Provider Catalog、Payment 模块；
 * Provider Catalog 尚未落地，Payment 已接（见 requestPayment）。
 */
@Service
public class BookingApplicationService {

    /**
     * 结算币种暂时固定为欧元——Booking 聚合目前没有按订单存币种的字段，多币种支持
     * 留给后续（见 docs/architecture/prd-alignment.md 里 Payment 相关缺口的讨论）。
     */
    private static final String DEFAULT_CURRENCY = "EUR";

    private final BookingRepository bookingRepository;
    private final DomainEventPublisher eventPublisher;
    private final PaymentPort paymentPort;

    public BookingApplicationService(BookingRepository bookingRepository,
                                      DomainEventPublisher eventPublisher,
                                      PaymentPort paymentPort) {
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.paymentPort = paymentPort;
    }

    @Transactional
    public Long createBooking(CreateBookingCommand command) {
        BookingOrder order = BookingOrder.create(
                command.patronId(),
                command.companionId(),
                command.skuId(),
                new TimeRange(command.start(), command.end()),
                command.priceSnapshot());

        BookingOrder saved = bookingRepository.save(order);
        eventPublisher.publish(new BookingCreatedEvent(
                saved.getId(), saved.getPatronId(), saved.getCompanionId()));
        return saved.getId();
    }

    /** 发起结账：把订单转 PENDING_PAYMENT，返回前端完成支付所需的 clientSecret。 */
    @Transactional
    public PaymentCheckoutResult requestPayment(Long bookingId) {
        BookingOrder order = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingException.notFound(bookingId));
        PaymentCheckoutResult checkout = paymentPort.requestPayment(
                order.getId(), order.getPatronId(), order.getPriceSnapshot(), DEFAULT_CURRENCY);
        // 重复调用本接口时 PaymentPort 会复用同一个未终态 intent，订单这边也只在还是 DRAFT 时迁移一次。
        if (order.getStatus() == BookingStatus.DRAFT) {
            order.requestPayment();
            bookingRepository.save(order);
        }
        return checkout;
    }

    /** 由 PaymentCapturedEventListener 在支付成功后调用，把订单推进到 PAID。 */
    @Transactional
    public void markPaid(Long bookingId) {
        BookingOrder order = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingException.notFound(bookingId));
        if (order.getStatus() == BookingStatus.PAID) {
            return;
        }
        order.markPaid();
        bookingRepository.save(order);
    }
}
