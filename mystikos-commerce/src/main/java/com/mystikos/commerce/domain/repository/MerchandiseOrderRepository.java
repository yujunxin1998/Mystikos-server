package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.common.result.PageResult;

import java.util.Optional;

public interface MerchandiseOrderRepository {

    Optional<MerchandiseOrder> findById(Long id);

    MerchandiseOrder save(MerchandiseOrder order);

    PageResult<MerchandiseOrder> findByPatronId(Long patronId, int pageNum, int pageSize);
}
