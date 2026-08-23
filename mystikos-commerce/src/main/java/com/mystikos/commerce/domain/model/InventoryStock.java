package com.mystikos.commerce.domain.model;

import com.mystikos.commerce.domain.CommerceException;

/** 库存聚合根，一个商品一行。下单预占、取消释放，不做超卖——预占失败直接拒单。 */
public class InventoryStock {

    private final Long productId;
    private int availableQty;
    private int reservedQty;

    private InventoryStock(Long productId, int availableQty, int reservedQty) {
        this.productId = productId;
        this.availableQty = availableQty;
        this.reservedQty = reservedQty;
    }

    public static InventoryStock restore(Long productId, int availableQty, int reservedQty) {
        return new InventoryStock(productId, availableQty, reservedQty);
    }

    public void reserve(int quantity) {
        if (availableQty < quantity) {
            throw CommerceException.insufficientStock(productId);
        }
        availableQty -= quantity;
        reservedQty += quantity;
    }

    /** 取消订单，把预占的库存放回可售数量。 */
    public void release(int quantity) {
        reservedQty -= quantity;
        availableQty += quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public int getReservedQty() {
        return reservedQty;
    }
}
