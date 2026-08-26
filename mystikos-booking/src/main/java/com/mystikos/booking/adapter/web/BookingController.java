package com.mystikos.booking.adapter.web;

import com.mystikos.booking.adapter.web.dto.CreateBookingRequest;
import com.mystikos.booking.adapter.web.dto.PaymentCheckoutResponse;
import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.booking.application.service.BookingOrderView;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.common.security.CurrentUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "预约撮合", description = "陪玩预约下单")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingApplicationService bookingApplicationService;

    public BookingController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @PostMapping
    @Operation(summary = "创建预约", description = "选陪玩+开始时间+时长（小时），价格按陪玩当前时薪服务端计算；"
            + "初始状态 DRAFT，15分钟内未完成支付会自动失效")
    public APIResponse<Long> create(@Valid @RequestBody CreateBookingRequest request) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        Long bookingId = bookingApplicationService.createBooking(new CreateBookingCommand(
                patronId,
                request.getCompanionId(),
                request.getStart(),
                request.getDurationHours()));
        return APIResponse.ok(bookingId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "预约详情", description = "只能查自己的订单；查询时会先懒同步过期状态，expiresAt 供前端倒计时")
    public APIResponse<BookingOrderView> get(@Parameter(description = "预约订单ID") @PathVariable Long id) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(bookingApplicationService.getBooking(id, patronId));
    }

    @GetMapping
    @Operation(summary = "我的预约列表", description = "按下单时间倒序分页")
    public APIResponse<PageResult<BookingOrderView>> listMine(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(bookingApplicationService.listMyBookings(patronId, pageNum, pageSize));
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "发起结账", description = "把预约订单转 PENDING_PAYMENT，返回前端用 Stripe.js 完成支付所需的 clientSecret；"
            + "订单已失效时返回错误")
    public APIResponse<PaymentCheckoutResponse> requestPayment(@Parameter(description = "预约订单ID") @PathVariable Long id) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(PaymentCheckoutResponse.from(bookingApplicationService.requestPayment(id, patronId)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消预约", description = "DRAFT/PENDING_PAYMENT/PAID 状态下允许取消")
    public APIResponse<Void> cancel(@Parameter(description = "预约订单ID") @PathVariable Long id) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        bookingApplicationService.cancelBooking(id, patronId);
        return APIResponse.ok();
    }
}
