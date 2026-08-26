package com.mystikos.booking.infrastructure.scheduler;

import com.mystikos.booking.application.service.BookingApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每分钟扫一次逾期未支付的订单并置为 EXPIRED。是"保底"机制——大多数订单会在
 * 用户主动查询详情/发起支付时被 BookingApplicationService 的懒同步先一步失效，
 * 这里只兜住用户从此不再打开这笔订单的情况，让状态最终一致。
 */
@Component
public class BookingExpirationScheduler {

    private final BookingApplicationService bookingApplicationService;

    public BookingExpirationScheduler(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void expireOverdueBookings() {
        bookingApplicationService.expireOverdueBookings();
    }
}
