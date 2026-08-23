package com.mystikos.commerce.domain.model;

/**
 * 购物车行。按 (patronId, productId) 唯一，不单独建"购物车"聚合包一层——
 * 购物车内各行之间没有需要一起维护的不变量，逐行操作即可。
 */
public class CartItem {

    private Long id;
    private final Long patronId;
    private final Long productId;
    private int quantity;

    private CartItem(Long id, Long patronId, Long productId, int quantity) {
        this.id = id;
        this.patronId = patronId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static CartItem create(Long patronId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }
        return new CartItem(null, patronId, productId, quantity);
    }

    public static CartItem restore(Long id, Long patronId, Long productId, int quantity) {
        return new CartItem(id, patronId, productId, quantity);
    }

    public void increaseQuantity(int delta) {
        this.quantity += delta;
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

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
