package com.mystikos.commerce.domain.model;

import java.math.BigDecimal;

/**
 * 订单行快照——下单时把商品名/单价复制一份，后续商品改价改名不影响历史订单，
 * 和 Booking 的 priceSnapshot 是同一个思路。
 */
public record OrderLineItem(Long productId, String productNameSnapshot, BigDecimal unitPriceSnapshot, int quantity) {

    public BigDecimal subtotal() {
        return unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }
}
