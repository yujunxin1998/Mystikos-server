package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

    Optional<CartItem> findByPatronAndProduct(Long patronId, Long productId);

    List<CartItem> findAllByPatron(Long patronId);

    CartItem save(CartItem cartItem);

    void deleteByPatronAndProduct(Long patronId, Long productId);

    void deleteAllByPatron(Long patronId);
}
