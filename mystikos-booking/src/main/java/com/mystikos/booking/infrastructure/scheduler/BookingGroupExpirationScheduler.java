package com.mystikos.booking.infrastructure.scheduler;

import com.mystikos.booking.application.service.BookingApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每分钟扫一次逾期未支付的预约组并级联失效所有子预约。是"保底"机制，同 BookingExpirationScheduler——
 * 大多数组会在用户主动查询详情/发起支付时被 BookingApplicationService 的懒同步先一步失效，
 * 这里只兜住用户从此不再打开这个组的情况。
 */
@Component
public class BookingGroupExpirationScheduler {

    private final BookingApplicationService bookingApplicationService;

    public BookingGroupExpirationScheduler(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void expireOverdueGroups() {
        bookingApplicationService.expireOverdueGroups();
    }
}
