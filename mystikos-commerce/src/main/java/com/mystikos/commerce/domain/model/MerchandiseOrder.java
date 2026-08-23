package com.mystikos.commerce.domain.model;

import com.mystikos.commerce.domain.CommerceException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MerchandiseOrder {

    private static final Set<OrderStatus> CANCELLABLE_FROM =
            EnumSet.of(OrderStatus.DRAFT, OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);

    private Long id;
    private final Long patronId;
    private final List<OrderLineItem> items;
    private final BigDecimal totalAmount;
    private final String shippingAddress;
    private OrderStatus status;
    private final OffsetDateTime createdAt;

    private MerchandiseOrder(Long id, Long patronId, List<OrderLineItem> items, BigDecimal totalAmount,
                              String shippingAddress, OrderStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.patronId = patronId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** 创建一笔新订单，初始状态 DRAFT——和 BookingOrder 一样，尚未接支付，先只做到创建这一步。 */
    public static MerchandiseOrder create(Long patronId, List<OrderLineItem> items, String shippingAddress) {
        if (items.isEmpty()) {
            throw CommerceException.orderEmpty();
        }
        BigDecimal total = items.stream().map(OrderLineItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MerchandiseOrder(null, patronId, items, total, shippingAddress, OrderStatus.DRAFT, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static MerchandiseOrder restore(Long id, Long patronId, List<OrderLineItem> items, BigDecimal totalAmount,
                                            String shippingAddress, OrderStatus status, OffsetDateTime createdAt) {
        return new MerchandiseOrder(id, patronId, items, totalAmount, shippingAddress, status, createdAt);
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

    public OrderStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
