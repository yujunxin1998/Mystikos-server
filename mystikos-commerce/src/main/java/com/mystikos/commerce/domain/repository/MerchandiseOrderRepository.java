package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.commerce.domain.model.OrderStatus;
import com.mystikos.common.result.PageResult;

import java.util.Optional;

public interface MerchandiseOrderRepository {

    Optional<MerchandiseOrder> findById(Long id);

    MerchandiseOrder save(MerchandiseOrder order);

    PageResult<MerchandiseOrder> findByPatronId(Long patronId, int pageNum, int pageSize);

    /** 后台分页查询订单，可按状态/买家过滤，均为空则不限。 */
    PageResult<MerchandiseOrder> findPage(OrderStatus status, Long patronId, int pageNum, int pageSize);
}
