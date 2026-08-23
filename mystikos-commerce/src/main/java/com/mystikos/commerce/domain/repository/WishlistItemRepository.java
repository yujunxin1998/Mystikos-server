package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.WishlistItem;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository {

    Optional<WishlistItem> findByPatronAndProduct(Long patronId, Long productId);

    List<WishlistItem> findAllByPatron(Long patronId);

    WishlistItem save(WishlistItem wishlistItem);

    void deleteByPatronAndProduct(Long patronId, Long productId);
}
