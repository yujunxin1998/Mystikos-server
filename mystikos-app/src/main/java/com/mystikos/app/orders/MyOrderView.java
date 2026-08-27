package com.mystikos.app.orders;

import com.mystikos.booking.application.service.BookingOrderView;
import com.mystikos.commerce.application.service.OrderView;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * “我的订单”聚合视图：陪玩订单（Booking）与商品订单（Commerce）是两个独立的限界上下文，
 * 状态机、字段都不通用，这里只在查询层按统一外观拼在一起，不做领域层合并——
 * 见 docs/architecture/prd-alignment.md“跨上下文一律经查询接口聚合，不做跨表 JOIN”。
 * booking/commerceOrder 两个字段互斥，具体看 orderType。
 */
public record MyOrderView(
        MyOrderType orderType,
        Long orderId,
        String status,
        BigDecimal amount,
        OffsetDateTime createdAt,
        BookingOrderView booking,
        OrderView commerceOrder
) {

    public static MyOrderView ofBooking(BookingOrderView view) {
        return new MyOrderView(MyOrderType.BOOKING, view.id(), view.status().name(), view.priceSnapshot(),
                view.createdAt(), view, null);
    }

    public static MyOrderView ofCommerce(OrderView view) {
        return new MyOrderView(MyOrderType.COMMERCE, view.orderId(), view.status().name(), view.totalAmount(),
                view.createdAt(), null, view);
    }
}
