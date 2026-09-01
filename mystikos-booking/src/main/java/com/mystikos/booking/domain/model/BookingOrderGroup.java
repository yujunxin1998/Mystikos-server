package com.mystikos.booking.domain.model;

import com.mystikos.booking.domain.BookingException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * 多条预约合并结算的"组"，一次支付覆盖组内所有 {@link BookingOrder} 子行
 * （子行通过 {@code BookingOrder.groupId} 指回这里）。组自己只管到 PAID 为止——
 * PAID 之后每个子预约各自独立走完整的 BookingStatus 流转，组不再参与，
 * 所以这里的状态机只有 DRAFT/PENDING_PAYMENT/PAID/EXPIRED/CANCELLED 五态，
 * 比 BookingStatus 小得多。
 *
 * <p>子行仍然保留自己完整的 BookingStatus——数据库的
 * {@code EXCLUDE USING gist (companion_id WITH =, tstzrange(start, end) WITH &&)}
 * 约束只能过滤子行自己的 status 列，防重必须继续认行级状态，不能把状态收敛到组上。
 */
public class BookingOrderGroup {

    /** 直接复用 BookingOrder 的支付有效期常量，不重复定义——两者本来就是同一条业务规则。 */
    public static final java.time.Duration PAYMENT_VALIDITY = BookingOrder.PAYMENT_VALIDITY;

    private static final Set<BookingGroupStatus> CANCELLABLE_FROM =
            EnumSet.of(BookingGroupStatus.DRAFT, BookingGroupStatus.PENDING_PAYMENT);
    private static final Set<BookingGroupStatus> EXPIRABLE_FROM =
            EnumSet.of(BookingGroupStatus.DRAFT, BookingGroupStatus.PENDING_PAYMENT);

    private Long id;
    private final Long patronId;
    private BookingGroupStatus status;
    private final BigDecimal totalAmount;
    private final OffsetDateTime createdAt;
    private Long version;

    private BookingOrderGroup(Long id, Long patronId, BookingGroupStatus status, BigDecimal totalAmount,
                               OffsetDateTime createdAt, Long version) {
        this.id = id;
        this.patronId = patronId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.version = version;
    }

    public static BookingOrderGroup create(Long patronId, BigDecimal totalAmount) {
        return new BookingOrderGroup(null, patronId, BookingGroupStatus.DRAFT, totalAmount, OffsetDateTime.now(), 0L);
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static BookingOrderGroup restore(Long id, Long patronId, BookingGroupStatus status, BigDecimal totalAmount,
                                             OffsetDateTime createdAt, Long version) {
        return new BookingOrderGroup(id, patronId, status, totalAmount, createdAt, version);
    }

    public boolean isOverdue(OffsetDateTime now) {
        return EXPIRABLE_FROM.contains(status) && now.isAfter(createdAt.plus(PAYMENT_VALIDITY));
    }

    public void expire() {
        if (!EXPIRABLE_FROM.contains(status)) {
            throw BookingException.statusInvalid("当前预约组状态 " + status + " 不允许标记为失效");
        }
        status = BookingGroupStatus.EXPIRED;
    }

    public void requestPayment() {
        transition(BookingGroupStatus.DRAFT, BookingGroupStatus.PENDING_PAYMENT);
    }

    public void markPaid() {
        transition(BookingGroupStatus.PENDING_PAYMENT, BookingGroupStatus.PAID);
    }

    public void cancel() {
        if (!CANCELLABLE_FROM.contains(status)) {
            throw BookingException.statusInvalid("当前预约组状态 " + status + " 不允许取消");
        }
        status = BookingGroupStatus.CANCELLED;
    }

    private void transition(BookingGroupStatus expected, BookingGroupStatus next) {
        if (status != expected) {
            throw BookingException.statusInvalid("期望预约组状态 " + expected + "，实际状态 " + status);
        }
        status = next;
    }

    public OffsetDateTime getExpiresAt() {
        return createdAt.plus(PAYMENT_VALIDITY);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public BookingGroupStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }
}
