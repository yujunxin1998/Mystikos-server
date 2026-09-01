package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.MerchandiseOrder;
import com.mystikos.commerce.domain.model.OrderStatus;
import com.mystikos.common.result.PageResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MerchandiseOrderRepository {

    Optional<MerchandiseOrder> findById(Long id);

    MerchandiseOrder save(MerchandiseOrder order);

    PageResult<MerchandiseOrder> findByPatronId(Long patronId, int pageNum, int pageSize);

    /** 后台分页查询订单，可按状态/买家过滤，均为空则不限。 */
    PageResult<MerchandiseOrder> findPage(OrderStatus status, Long patronId, int pageNum, int pageSize);

    /** DRAFT/PENDING_PAYMENT 且早于 cutoff 创建的订单，供定时任务批量置为 EXPIRED。 */
    List<MerchandiseOrder> findExpirable(OffsetDateTime cutoff);
}
