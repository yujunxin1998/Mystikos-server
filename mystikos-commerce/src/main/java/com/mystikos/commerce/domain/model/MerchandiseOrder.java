package com.mystikos.commerce.domain.model;

import com.mystikos.commerce.domain.CommerceException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MerchandiseOrder {

    /**
     * 下单后的支付有效期，同 BookingOrder.PAYMENT_VALIDITY——两个聚合各自定义常量，
     * 不共用一份：commerce/booking 是两个独立模块，值恰好相同不代表要耦合在一起维护。
     */
    public static final Duration PAYMENT_VALIDITY = Duration.ofMinutes(15);

    private static final Set<OrderStatus> CANCELLABLE_FROM =
            EnumSet.of(OrderStatus.DRAFT, OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
    private static final Set<OrderStatus> REFUNDABLE_FROM =
            EnumSet.of(OrderStatus.PAID, OrderStatus.FULFILLING, OrderStatus.SHIPPED, OrderStatus.COMPLETED);
    private static final Set<OrderStatus> EXPIRABLE_FROM =
            EnumSet.of(OrderStatus.DRAFT, OrderStatus.PENDING_PAYMENT);

    private Long id;
    private final Long patronId;
    private final List<OrderLineItem> items;
    private final BigDecimal totalAmount;
    private final String shippingAddress;
    private final Long shippingAddressId;
    private OrderStatus status;
    private final OffsetDateTime createdAt;

    private MerchandiseOrder(Long id, Long patronId, List<OrderLineItem> items, BigDecimal totalAmount,
                              String shippingAddress, Long shippingAddressId, OrderStatus status,
                              OffsetDateTime createdAt) {
        this.id = id;
        this.patronId = patronId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.shippingAddressId = shippingAddressId;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** 创建一笔新订单，初始状态 DRAFT——和 BookingOrder 一样，尚未接支付，先只做到创建这一步。 */
    public static MerchandiseOrder create(Long patronId, List<OrderLineItem> items, String shippingAddress,
                                           Long shippingAddressId) {
        if (items.isEmpty()) {
            throw CommerceException.orderEmpty();
        }
        BigDecimal total = items.stream().map(OrderLineItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MerchandiseOrder(null, patronId, items, total, shippingAddress, shippingAddressId,
                OrderStatus.DRAFT, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static MerchandiseOrder restore(Long id, Long patronId, List<OrderLineItem> items, BigDecimal totalAmount,
                                            String shippingAddress, Long shippingAddressId, OrderStatus status,
                                            OffsetDateTime createdAt) {
        return new MerchandiseOrder(id, patronId, items, totalAmount, shippingAddress, shippingAddressId, status,
                createdAt);
    }

    /** 距下单已过 15 分钟且仍处于未支付状态，判定为已失效——由调用方决定是否落库 {@link #expire()}。 */
    public boolean isOverdue(OffsetDateTime now) {
        return EXPIRABLE_FROM.contains(status) && now.isAfter(createdAt.plus(PAYMENT_VALIDITY));
    }

    /** 把逾期未支付的订单标记为失效；只能从 {@link #EXPIRABLE_FROM} 里的状态迁移，供定时任务/懒检查复用。 */
    public void expire() {
        if (!EXPIRABLE_FROM.contains(status)) {
            throw CommerceException.statusInvalid("当前状态 " + status + " 不允许标记为失效");
        }
        status = OrderStatus.EXPIRED;
    }

    /** 支付有效期截止时刻，供前端展示倒计时用；跟 {@link #isOverdue} 的判定口径一致。 */
    public OffsetDateTime getExpiresAt() {
        return createdAt.plus(PAYMENT_VALIDITY);
    }

    public void requestPayment() {
        transition(OrderStatus.DRAFT, OrderStatus.PENDING_PAYMENT);
    }

    public void markPaid() {
        transition(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
    }

    public void startFulfilling() {
        transition(OrderStatus.PAID, OrderStatus.FULFILLING);
    }

    public void ship() {
        transition(OrderStatus.FULFILLING, OrderStatus.SHIPPED);
    }

    public void complete() {
        transition(OrderStatus.SHIPPED, OrderStatus.COMPLETED);
    }

    public void cancel() {
        if (!CANCELLABLE_FROM.contains(status)) {
            throw CommerceException.statusInvalid("当前状态 " + status + " 不允许取消");
        }
        status = OrderStatus.CANCELLED;
    }

    public void refund() {
        if (!REFUNDABLE_FROM.contains(status)) {
            throw CommerceException.statusInvalid("当前状态 " + status + " 不允许退款");
        }
        status = OrderStatus.REFUNDED;
    }

    private void transition(OrderStatus expected, OrderStatus next) {
        if (status != expected) {
            throw CommerceException.statusInvalid("期望状态 " + expected + "，实际状态 " + status);
        }
        status = next;
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

    public List<OrderLineItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
