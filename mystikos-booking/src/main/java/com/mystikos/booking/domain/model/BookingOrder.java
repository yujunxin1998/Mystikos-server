package com.mystikos.booking.domain.model;

import com.mystikos.booking.domain.BookingException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * 预约订单聚合根。状态迁移全部收在聚合内部，保证任何调用方都不能绕过状态机
 * 直接把订单改到非法状态；同一时段防重不在这里做（应用层无法保证并发安全），
 * 交给数据库的 EXCLUDE 约束兜底。
 */
public class BookingOrder {

    /**
     * 下单后的支付有效期：超过这个时长仍未支付，订单自动失效，需要重新下单。
     * 公开给应用层的定时任务（BookingApplicationService#expireOverdueBookings）复用，
     * 避免"15 分钟"这个业务规则在两处各写一份容易漂移。
     */
    public static final Duration PAYMENT_VALIDITY = Duration.ofMinutes(15);

    private static final Set<BookingStatus> CANCELLABLE_FROM =
            EnumSet.of(BookingStatus.DRAFT, BookingStatus.PENDING_PAYMENT, BookingStatus.PAID);
    private static final Set<BookingStatus> EXPIRABLE_FROM =
            EnumSet.of(BookingStatus.DRAFT, BookingStatus.PENDING_PAYMENT);

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private final TimeRange timeRange;
    private final BigDecimal durationHours;
    private final BigDecimal priceSnapshot;
    private BookingStatus status;
    private final OffsetDateTime createdAt;
    private Long version;

    private BookingOrder(Long id, Long patronId, Long companionId, TimeRange timeRange,
                          BigDecimal durationHours, BigDecimal priceSnapshot,
                          BookingStatus status, OffsetDateTime createdAt, Long version) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.timeRange = timeRange;
        this.durationHours = durationHours;
        this.priceSnapshot = priceSnapshot;
        this.status = status;
        this.createdAt = createdAt;
        this.version = version;
    }

    /** 创建一笔新预约，初始状态为 DRAFT，下单时刻即开始计算 15 分钟支付有效期。 */
    public static BookingOrder create(Long patronId, Long companionId, TimeRange timeRange,
                                       BigDecimal durationHours, BigDecimal priceSnapshot) {
        return new BookingOrder(null, patronId, companionId, timeRange, durationHours, priceSnapshot,
                BookingStatus.DRAFT, OffsetDateTime.now(), 0L);
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static BookingOrder restore(Long id, Long patronId, Long companionId, TimeRange timeRange,
                                        BigDecimal durationHours, BigDecimal priceSnapshot,
                                        BookingStatus status, OffsetDateTime createdAt, Long version) {
        return new BookingOrder(id, patronId, companionId, timeRange, durationHours, priceSnapshot,
                status, createdAt, version);
    }

    /** 距下单已过 15 分钟且仍处于未支付状态，判定为已失效——由调用方决定是否落库 {@link #expire()}。 */
    public boolean isOverdue(OffsetDateTime now) {
        return EXPIRABLE_FROM.contains(status) && now.isAfter(createdAt.plus(PAYMENT_VALIDITY));
    }

    /** 把逾期未支付的订单标记为失效；只能从 {@link #EXPIRABLE_FROM} 里的状态迁移，供定时任务/懒检查复用。 */
    public void expire() {
        if (!EXPIRABLE_FROM.contains(status)) {
            throw BookingException.statusInvalid("当前状态 " + status + " 不允许标记为失效");
        }
        status = BookingStatus.EXPIRED;
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

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public BigDecimal getDurationHours() {
        return durationHours;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /** 支付有效期截止时刻，供前端展示倒计时用；跟 {@link #isOverdue} 的判定口径一致。 */
    public OffsetDateTime getExpiresAt() {
        return createdAt.plus(PAYMENT_VALIDITY);
    }

    public Long getVersion() {
        return version;
    }
}
