package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.MerchandiseOrder;

import java.util.Optional;

public interface MerchandiseOrderRepository {

    Optional<MerchandiseOrder> findById(Long id);

    MerchandiseOrder save(MerchandiseOrder order);
}
