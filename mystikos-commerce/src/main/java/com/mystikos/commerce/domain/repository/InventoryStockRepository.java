package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.InventoryStock;

import java.util.Optional;

public interface InventoryStockRepository {

    Optional<InventoryStock> findByProductId(Long productId);

    InventoryStock save(InventoryStock stock);
}
