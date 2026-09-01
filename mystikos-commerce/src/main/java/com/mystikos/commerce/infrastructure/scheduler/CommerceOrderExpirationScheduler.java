package com.mystikos.commerce.infrastructure.scheduler;

import com.mystikos.commerce.application.service.CommerceApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每分钟扫一次逾期未支付的商城订单并置为 EXPIRED（同时释放预占库存）。是"保底"机制——
 * 大多数订单会在用户主动查询详情/发起支付时被 CommerceApplicationService 的懒同步先一步失效，
 * 这里只兜住用户从此不再打开这笔订单的情况，让状态最终一致。同 BookingExpirationScheduler。
 */
@Component
public class CommerceOrderExpirationScheduler {

    private final CommerceApplicationService commerceApplicationService;

    public CommerceOrderExpirationScheduler(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void expireOverdueOrders() {
        commerceApplicationService.expireOverdueOrders();
    }
}
