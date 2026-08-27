package com.mystikos.app.orders;

import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.booking.application.service.BookingOrderView;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.commerce.application.service.OrderView;
import com.mystikos.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 聚合 Booking（陪玩订单）与 Commerce（商品订单）各自的“我的订单”分页查询，按下单时间倒序
 * 合并后再切页——两边各自的 findByPatronId 分页是权威来源，这里不做跨库 JOIN，只在查询层拼接。
 * 简化实现：向两边各要第 1..pageNum 页那么多条（pageNum*pageSize），merge-sort 后切出目标页；
 * 深翻页时每页都会重新拉取前面所有页的数据，量级放大后如果需要可以换成游标分页。
 */
@Service
public class MyOrderApplicationService {

    private final BookingApplicationService bookingApplicationService;
    private final CommerceApplicationService commerceApplicationService;

    public MyOrderApplicationService(BookingApplicationService bookingApplicationService,
                                      CommerceApplicationService commerceApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
        this.commerceApplicationService = commerceApplicationService;
    }

    public PageResult<MyOrderView> listMyOrders(Long patronId, int pageNum, int pageSize) {
        int fetchSize = pageNum * pageSize;
        PageResult<BookingOrderView> bookingPage = bookingApplicationService.listMyBookings(patronId, 1, fetchSize);
        PageResult<OrderView> commercePage = commerceApplicationService.listMyOrders(patronId, 1, fetchSize);

        List<MyOrderView> merged = new ArrayList<>(bookingPage.records().size() + commercePage.records().size());
        bookingPage.records().forEach(view -> merged.add(MyOrderView.ofBooking(view)));
        commercePage.records().forEach(view -> merged.add(MyOrderView.ofCommerce(view)));
        merged.sort(Comparator.comparing(MyOrderView::createdAt).reversed());

        int fromIndex = Math.min((pageNum - 1) * pageSize, merged.size());
        int toIndex = Math.min(fromIndex + pageSize, merged.size());
        long total = bookingPage.total() + commercePage.total();
        return PageResult.of(merged.subList(fromIndex, toIndex), total, pageNum, pageSize);
    }
}
