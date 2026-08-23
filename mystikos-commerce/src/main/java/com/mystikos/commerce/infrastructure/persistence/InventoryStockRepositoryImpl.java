package com.mystikos.commerce.infrastructure.persistence;

import com.mystikos.commerce.domain.model.InventoryStock;
import com.mystikos.commerce.domain.repository.InventoryStockRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InventoryStockRepositoryImpl implements InventoryStockRepository {

    private final InventoryStockMapper mapper;

    public InventoryStockRepositoryImpl(InventoryStockMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<InventoryStock> findByProductId(Long productId) {
        return Optional.ofNullable(mapper.selectById(productId))
                .map(po -> InventoryStock.restore(po.getProductId(), po.getAvailableQty(), po.getReservedQty()));
    }

    @Override
    public InventoryStock save(InventoryStock stock) {
        InventoryStockPO po = new InventoryStockPO();
        po.setProductId(stock.getProductId());
        po.setAvailableQty(stock.getAvailableQty());
        po.setReservedQty(stock.getReservedQty());
        mapper.updateById(po);
        return stock;
    }
}
