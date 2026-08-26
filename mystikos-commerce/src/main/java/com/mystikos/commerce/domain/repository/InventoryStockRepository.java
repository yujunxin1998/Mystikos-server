package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.InventoryStock;

import java.util.Optional;

public interface InventoryStockRepository {

    Optional<InventoryStock> findByProductId(Long productId);

    /** 只做更新（reserve/release 场景），行必须已存在；新增商品初始化库存行用 {@link #insert}。 */
    InventoryStock save(InventoryStock stock);

    /** 新增商品时插入初始库存行，productId 由调用方指定（主键策略是 INPUT，不自增）。 */
    void insert(InventoryStock stock);
}
