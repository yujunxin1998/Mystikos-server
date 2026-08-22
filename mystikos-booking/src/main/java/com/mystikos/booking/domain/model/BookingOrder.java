package com.mystikos.booking.domain.model;

import com.mystikos.booking.domain.BookingException;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/**
 * 预约订单聚合根。状态迁移全部收在聚合内部，保证任何调用方都不能绕过状态机
 * 直接把订单改到非法状态；同一时段防重不在这里做（应用层无法保证并发安全），
 * 交给数据库的 EXCLUDE 约束兜底。
 */
public class BookingOrder {

    private static final Set<BookingStatus> CANCELLABLE_FROM =
            EnumSet.of(BookingStatus.DRAFT, BookingStatus.PENDING_PAYMENT, BookingStatus.PAID);

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private final Long skuId;
    private final TimeRange timeRange;
    private final BigDecimal priceSnapshot;
    private BookingStatus status;
    private Long version;

    private BookingOrder(Long id, Long patronId, Long companionId, Long skuId,
                          TimeRange timeRange, BigDecimal priceSnapshot,
                          BookingStatus status, Long version) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.skuId = skuId;
        this.timeRange = timeRange;
        this.priceSnapshot = priceSnapshot;
        this.status = status;
        this.version = version;
    }

    /** 创建一笔新预约，初始状态为 DRAFT。 */
    public static BookingOrder create(Long patronId, Long companionId, Long skuId,
                                       TimeRange timeRange, BigDecimal priceSnapshot) {
        return new BookingOrder(null, patronId, companionId, skuId, timeRange, priceSnapshot,
                BookingStatus.DRAFT, 0L);
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static BookingOrder restore(Long id, Long patronId, Long companionId, Long skuId,
                                        TimeRange timeRange, BigDecimal priceSnapshot,
                                        BookingStatus status, Long version) {
        return new BookingOrder(id, patronId, companionId, skuId, timeRange, priceSnapshot, status, version);
    }

    public void requestPayment() {
        transition(BookingStatus.DRAFT, BookingStatus.PENDING_PAYMENT);
    }

    public void markPaid() {
        transition(BookingStatus.PENDING_PAYMENT, BookingStatus.PAID);
    }

    public void startMatching() {
        transition(BookingStatus.PAID, BookingStatus.MATCHING);
    }

    public void accept() {
        transition(BookingStatus.MATCHING, BookingStatus.ACCEPTED);
    }

    public void startService() {
        transition(BookingStatus.ACCEPTED, BookingStatus.IN_SERVICE);
    }

    public void complete() {
        transition(BookingStatus.IN_SERVICE, BookingStatus.COMPLETED);
    }

    public void cancel() {
        if (!CANCELLABLE_FROM.contains(status)) {
            throw BookingException.statusInvalid("当前状态 " + status + " 不允许取消");
        }
        status = BookingStatus.CANCELLED;
    }

    private void transition(BookingStatus expected, BookingStatus next) {
        if (status != expected) {
            throw BookingException.statusInvalid(
                    "期望状态 " + expected + "，实际状态 " + status);
        }
        status = next;
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}
