package com.mystikos.booking.application.service;

import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.application.port.CompanionPricingPort;
import com.mystikos.booking.application.port.CompanionPricingSnapshot;
import com.mystikos.booking.application.port.PaymentCheckoutResult;
import com.mystikos.booking.application.port.PaymentPort;
import com.mystikos.booking.domain.BookingException;
import com.mystikos.booking.domain.event.BookingCreatedEvent;
import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingRepository;
import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 预约撮合用例编排。跨限界上下文的调用（校验陪玩定价/档期、发起支付）
 * 经 application/port 接口对接 Provider Catalog 语义上归属的 Identity（陪玩定价）、
 * Payment 模块；PAID 之后的流转方法仍留在聚合上没接用例。
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
    private final CompanionPricingPort companionPricingPort;

    public BookingApplicationService(BookingRepository bookingRepository,
                                      DomainEventPublisher eventPublisher,
                                      PaymentPort paymentPort,
                                      CompanionPricingPort companionPricingPort) {
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.paymentPort = paymentPort;
        this.companionPricingPort = companionPricingPort;
    }

    /** 创建预约：按陪玩当前时薪 × 时长权威算价，不信任客户端传入的价格。 */
    @Transactional
    public Long createBooking(CreateBookingCommand command) {
        CompanionPricingSnapshot pricing = companionPricingPort.getPricing(command.companionId());
        if (!pricing.bookable()) {
            throw BookingException.companionNotBookable(command.companionId());
        }

        long durationMinutes = command.durationHours().multiply(BigDecimal.valueOf(60)).longValueExact();
        OffsetDateTime end = command.start().plusMinutes(durationMinutes);
        BigDecimal priceSnapshot = pricing.hourlyRate().multiply(command.durationHours())
                .setScale(2, RoundingMode.HALF_UP);

        BookingOrder order = BookingOrder.create(
                command.patronId(),
                command.companionId(),
                new TimeRange(command.start(), end),
                command.durationHours(),
                priceSnapshot);

        BookingOrder saved = bookingRepository.save(order);
        eventPublisher.publish(new BookingCreatedEvent(
                saved.getId(), saved.getPatronId(), saved.getCompanionId()));
        return saved.getId();
    }

    /** 发起结账：把预约订单转 PENDING_PAYMENT，返回前端完成支付所需的 clientSecret。 */
    @Transactional
    public PaymentCheckoutResult requestPayment(Long bookingId, Long patronId) {
        BookingOrder order = loadOwnedAndSyncExpiry(bookingId, patronId);
        if (order.getStatus() == BookingStatus.EXPIRED) {
            throw BookingException.expired(bookingId);
        }
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

    /** 订单详情：读取时先懒同步过期状态，不用等定时任务下一轮才反映真实状态。 */
    @Transactional
    public BookingOrderView getBooking(Long bookingId, Long patronId) {
        return BookingOrderView.from(loadOwnedAndSyncExpiry(bookingId, patronId));
    }

    /** 我的订单列表，按下单时间倒序分页。 */
    public PageResult<BookingOrderView> listMyBookings(Long patronId, int pageNum, int pageSize) {
        PageResult<BookingOrder> page = bookingRepository.findByPatronId(patronId, pageNum, pageSize);
        List<BookingOrderView> views = page.records().stream().map(BookingOrderView::from).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

    /** 老板主动取消，只允许 DRAFT/PENDING_PAYMENT/PAID（见 BookingOrder#cancel）。 */
    @Transactional
    public void cancelBooking(Long bookingId, Long patronId) {
        BookingOrder order = loadOwnedAndSyncExpiry(bookingId, patronId);
        order.cancel();
        bookingRepository.save(order);
    }

    /** 定时任务入口：把支付有效期已过的 DRAFT/PENDING_PAYMENT 订单批量置为 EXPIRED。 */
    @Transactional
    public void expireOverdueBookings() {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(BookingOrder.PAYMENT_VALIDITY);
        for (BookingOrder order : bookingRepository.findExpirable(cutoff)) {
            order.expire();
            bookingRepository.save(order);
        }
    }

    /**
     * 取订单并校验归属；不属于该老板的订单一律当"不存在"处理，不暴露他人订单是否存在。
     * 顺带把逾期未支付的订单懒失效并落库，保证任何读到的状态都是最新的。
     */
    private BookingOrder loadOwnedAndSyncExpiry(Long bookingId, Long patronId) {
        BookingOrder order = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingException.notFound(bookingId));
        if (!order.getPatronId().equals(patronId)) {
            throw BookingException.notFound(bookingId);
        }
        if (order.isOverdue(OffsetDateTime.now())) {
            order.expire();
            bookingRepository.save(order);
        }
        return order;
    }
}
