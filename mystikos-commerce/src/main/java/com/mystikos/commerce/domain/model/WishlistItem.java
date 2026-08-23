package com.mystikos.commerce.domain.model;

import java.time.OffsetDateTime;

public class WishlistItem {

    private Long id;
    private final Long patronId;
    private final Long productId;
    private final OffsetDateTime addedAt;

    private WishlistItem(Long id, Long patronId, Long productId, OffsetDateTime addedAt) {
        this.id = id;
        this.patronId = patronId;
        this.productId = productId;
        this.addedAt = addedAt;
    }

    public static WishlistItem create(Long patronId, Long productId) {
        return new WishlistItem(null, patronId, productId, OffsetDateTime.now());
    }

    public static WishlistItem restore(Long id, Long patronId, Long productId, OffsetDateTime addedAt) {
        return new WishlistItem(id, patronId, productId, addedAt);
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

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }
}
