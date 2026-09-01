package com.mystikos.booking.application.service;

import com.mystikos.booking.application.command.AddBookingCartLineCommand;
import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.application.port.CompanionPricingPort;
import com.mystikos.booking.application.port.CompanionPricingSnapshot;
import com.mystikos.booking.application.port.PaymentCheckoutResult;
import com.mystikos.booking.application.port.PaymentPort;
import com.mystikos.booking.domain.BookingException;
import com.mystikos.booking.domain.event.BookingCreatedEvent;
import com.mystikos.booking.domain.model.BookingCartLine;
import com.mystikos.booking.domain.model.BookingGroupStatus;
import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingOrderGroup;
import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingCartLineRepository;
import com.mystikos.booking.domain.repository.BookingOrderGroupRepository;
import com.mystikos.booking.domain.repository.BookingRepository;
import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.common.result.PageResult;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final BookingCartLineRepository bookingCartLineRepository;
    private final BookingOrderGroupRepository bookingOrderGroupRepository;
    private final DomainEventPublisher eventPublisher;
    private final PaymentPort paymentPort;
    private final CompanionPricingPort companionPricingPort;

    public BookingApplicationService(BookingRepository bookingRepository,
                                      BookingCartLineRepository bookingCartLineRepository,
                                      BookingOrderGroupRepository bookingOrderGroupRepository,
                                      DomainEventPublisher eventPublisher,
                                      PaymentPort paymentPort,
                                      CompanionPricingPort companionPricingPort) {
        this.bookingRepository = bookingRepository;
        this.bookingCartLineRepository = bookingCartLineRepository;
        this.bookingOrderGroupRepository = bookingOrderGroupRepository;
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

    /**
     * 发起结账：把预约订单转 PENDING_PAYMENT，返回前端完成支付所需的 payload。
     * 结算币种固定欧元（见类注释），选支付宝/微信时网关会因为币种不是 CNY 直接拒绝——
     * 在给预约订单接入人民币定价之前，这是预期内的限制，不是 bug。
     */
    @Transactional
    public PaymentCheckoutResult requestPayment(Long bookingId, Long patronId, PaymentProvider provider, PaymentScene scene) {
        BookingOrder order = loadOwnedAndSyncExpiry(bookingId, patronId);
        if (order.getStatus() == BookingStatus.EXPIRED) {
            throw BookingException.expired(bookingId);
        }
        PaymentCheckoutResult checkout = paymentPort.requestPayment(
                order.getId(), order.getPatronId(), order.getPriceSnapshot(), DEFAULT_CURRENCY, provider, scene);
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

    /** 定时任务入口：把支付有效期已过的 DRAFT/PENDING_PAYMENT 独立订单批量置为 EXPIRED。 */
    @Transactional
    public void expireOverdueBookings() {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(BookingOrder.PAYMENT_VALIDITY);
        for (BookingOrder order : bookingRepository.findExpirable(cutoff)) {
            order.expire();
            bookingRepository.save(order);
        }
    }

    @Transactional
    public Long addToBookingCart(AddBookingCartLineCommand command) {
        long durationMinutes = command.durationHours().multiply(BigDecimal.valueOf(60)).longValueExact();
        OffsetDateTime end = command.start().plusMinutes(durationMinutes);
        BookingCartLine line = BookingCartLine.create(command.patronId(), command.companionId(),
                new TimeRange(command.start(), end), command.durationHours());
        return bookingCartLineRepository.save(line).getId();
    }

    /** 预约购物车列表：每行实时查陪玩当前定价算预估价，不用购物车里过期的快照。 */
    public List<BookingCartLineView> listBookingCart(Long patronId) {
        return bookingCartLineRepository.findAllByPatron(patronId).stream()
                .map(line -> {
                    CompanionPricingSnapshot pricing = companionPricingPort.getPricing(line.getCompanionId());
                    BigDecimal estimatedPrice = pricing.hourlyRate().multiply(line.getDurationHours())
                            .setScale(2, RoundingMode.HALF_UP);
                    return new BookingCartLineView(line.getId(), line.getCompanionId(), line.getTimeRange().start(),
                            line.getTimeRange().end(), line.getDurationHours(), estimatedPrice, pricing.bookable());
                })
                .toList();
    }

    @Transactional
    public void removeFromBookingCart(Long patronId, Long lineId) {
        bookingCartLineRepository.deleteByPatronAndIds(patronId, List.of(lineId));
    }

    /**
     * 结算购物车里选中的行：逐行按陪玩当前时薪权威算价并创建归属同一个新预约组的子预约，
     * 汇总总价，同事务内保存组+所有子行，成功后才清掉选中的购物车行。任意一行的陪玩+时段
     * 跟已有预约冲突（数据库 EXCLUDE 约束）就整单回滚，不留下部分创建的组。
     */
    @Transactional
    public Long checkoutBookingCart(Long patronId, List<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) {
            throw BookingException.statusInvalid("请至少选择一条预约");
        }
        List<BookingCartLine> lines = bookingCartLineRepository.findByIdsAndPatron(patronId, lineIds);
        Map<Long, BookingCartLine> linesById = lines.stream()
                .collect(Collectors.toMap(BookingCartLine::getId, line -> line));
        List<BookingCartLine> selected = lineIds.stream()
                .map(id -> {
                    BookingCartLine line = linesById.get(id);
                    if (line == null) {
                        throw BookingException.cartLineNotFound(id);
                    }
                    return line;
                })
                .toList();

        Map<Long, BigDecimal> priceByLineId = selected.stream()
                .collect(Collectors.toMap(BookingCartLine::getId, line -> {
                    CompanionPricingSnapshot pricing = companionPricingPort.getPricing(line.getCompanionId());
                    if (!pricing.bookable()) {
                        throw BookingException.companionNotBookable(line.getCompanionId());
                    }
                    return pricing.hourlyRate().multiply(line.getDurationHours()).setScale(2, RoundingMode.HALF_UP);
                }));
        BigDecimal totalAmount = priceByLineId.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BookingOrderGroup group = bookingOrderGroupRepository.save(BookingOrderGroup.create(patronId, totalAmount));

        try {
            for (BookingCartLine line : selected) {
                BookingOrder order = BookingOrder.createGrouped(patronId, line.getCompanionId(), line.getTimeRange(),
                        line.getDurationHours(), priceByLineId.get(line.getId()), group.getId());
                BookingOrder saved = bookingRepository.save(order);
                eventPublisher.publish(new BookingCreatedEvent(saved.getId(), saved.getPatronId(), saved.getCompanionId()));
            }
        } catch (DataIntegrityViolationException e) {
            throw BookingException.slotConflict();
        }

        bookingCartLineRepository.deleteByPatronAndIds(patronId, lineIds);
        return group.getId();
    }

    /**
     * 发起预约组结账：把组转 PENDING_PAYMENT 并级联到所有子预约，返回前端完成支付所需的 payload。
     * 结算币种固定欧元，选支付宝/微信时网关会因为币种不是 CNY 直接拒绝，同单条预约的限制。
     */
    @Transactional
    public PaymentCheckoutResult requestGroupPayment(Long groupId, Long patronId, PaymentProvider provider, PaymentScene scene) {
        BookingOrderGroup group = loadOwnedGroupAndSyncExpiry(groupId, patronId);
        if (group.getStatus() == BookingGroupStatus.EXPIRED) {
            throw BookingException.expired(groupId);
        }
        PaymentCheckoutResult checkout = paymentPort.requestGroupPayment(
                group.getId(), group.getPatronId(), group.getTotalAmount(), DEFAULT_CURRENCY, provider, scene);
        if (group.getStatus() == BookingGroupStatus.DRAFT) {
            group.requestPayment();
            bookingOrderGroupRepository.save(group);
            for (BookingOrder child : bookingRepository.findByGroupId(groupId)) {
                if (child.getStatus() == BookingStatus.DRAFT) {
                    child.requestPayment();
                    bookingRepository.save(child);
                }
            }
        }
        return checkout;
    }

    /** 由 PaymentCapturedEventListener 在预约组支付成功后调用，级联把组和所有子预约推进到 PAID。 */
    @Transactional
    public void markGroupPaid(Long groupId) {
        BookingOrderGroup group = bookingOrderGroupRepository.findById(groupId)
                .orElseThrow(() -> BookingException.groupNotFound(groupId));
        if (group.getStatus() != BookingGroupStatus.PAID) {
            group.markPaid();
            bookingOrderGroupRepository.save(group);
        }
        for (BookingOrder child : bookingRepository.findByGroupId(groupId)) {
            if (child.getStatus() != BookingStatus.PAID) {
                child.markPaid();
                bookingRepository.save(child);
            }
        }
    }

    /** 预约组详情：懒同步过期状态后返回组信息+组内所有子预约。 */
    @Transactional
    public BookingOrderGroupView getBookingGroup(Long groupId, Long patronId) {
        BookingOrderGroup group = loadOwnedGroupAndSyncExpiry(groupId, patronId);
        List<BookingOrderView> bookings = bookingRepository.findByGroupId(groupId).stream()
                .map(BookingOrderView::from)
                .toList();
        return new BookingOrderGroupView(group.getId(), group.getStatus(), group.getTotalAmount(),
                group.getCreatedAt(), group.getExpiresAt(), bookings);
    }

    /** 取消预约组：只允许 DRAFT/PENDING_PAYMENT，PAID 之后不再有组级操作，级联取消所有子预约。 */
    @Transactional
    public void cancelGroup(Long groupId, Long patronId) {
        BookingOrderGroup group = loadOwnedGroupAndSyncExpiry(groupId, patronId);
        group.cancel();
        bookingOrderGroupRepository.save(group);
        for (BookingOrder child : bookingRepository.findByGroupId(groupId)) {
            child.cancel();
            bookingRepository.save(child);
        }
    }

    /** 定时任务入口：把支付有效期已过的预约组批量置为 EXPIRED，并级联失效所有子预约。 */
    @Transactional
    public void expireOverdueGroups() {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(BookingOrderGroup.PAYMENT_VALIDITY);
        for (BookingOrderGroup group : bookingOrderGroupRepository.findExpirableGroups(cutoff)) {
            group.expire();
            bookingOrderGroupRepository.save(group);
            for (BookingOrder child : bookingRepository.findByGroupId(group.getId())) {
                if (child.getStatus() == BookingStatus.DRAFT || child.getStatus() == BookingStatus.PENDING_PAYMENT) {
                    child.expire();
                    bookingRepository.save(child);
                }
            }
        }
    }

    /**
     * 取预约组并校验归属；不属于该老板的组一律当"不存在"处理。顺带把逾期未支付的组懒失效
     * 并落库+级联子预约，保证任何读到的状态都是最新的。
     */
    private BookingOrderGroup loadOwnedGroupAndSyncExpiry(Long groupId, Long patronId) {
        BookingOrderGroup group = bookingOrderGroupRepository.findById(groupId)
                .orElseThrow(() -> BookingException.groupNotFound(groupId));
        if (!group.getPatronId().equals(patronId)) {
            throw BookingException.groupNotFound(groupId);
        }
        if (group.isOverdue(OffsetDateTime.now())) {
            group.expire();
            bookingOrderGroupRepository.save(group);
            for (BookingOrder child : bookingRepository.findByGroupId(groupId)) {
                if (child.getStatus() == BookingStatus.DRAFT || child.getStatus() == BookingStatus.PENDING_PAYMENT) {
                    child.expire();
                    bookingRepository.save(child);
                }
            }
        }
        return group;
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
