package com.mystikos.booking.adapter.web;

import com.mystikos.booking.adapter.web.dto.AddBookingCartLineRequest;
import com.mystikos.booking.adapter.web.dto.CheckoutBookingCartRequest;
import com.mystikos.booking.application.command.AddBookingCartLineCommand;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.booking.application.service.BookingCartLineView;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预约购物车：加入多条待选预约（不同陪玩/不同时段），结算时选一部分/全部合并成一个预约组
 * 一次支付，见 {@link BookingGroupController}。这里不立即占用陪玩档期——真正的防重
 * 发生在结算落单那一刻（数据库 EXCLUDE 约束），购物车里的行只是草稿。
 */
@RestController
@RequestMapping("/api/v1/booking-cart")
@Tag(name = "预约撮合", description = "预约购物车")
@SecurityRequirement(name = "bearerAuth")
public class BookingCartController {

    private final BookingApplicationService bookingApplicationService;

    public BookingCartController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @GetMapping
    @Operation(summary = "我的预约购物车", description = "每行实时按陪玩当前定价算预估价")
    public APIResponse<List<BookingCartLineView>> list() {
        return APIResponse.ok(bookingApplicationService.listBookingCart(currentPatronId()));
    }

    @PostMapping
    @Operation(summary = "加入预约购物车")
    public APIResponse<Long> add(@Valid @RequestBody AddBookingCartLineRequest request) {
        Long lineId = bookingApplicationService.addToBookingCart(new AddBookingCartLineCommand(
                currentPatronId(), request.getCompanionId(), request.getStart(), request.getDurationHours()));
        return APIResponse.ok(lineId);
    }

    @DeleteMapping("/{lineId}")
    @Operation(summary = "移出预约购物车")
    public APIResponse<Void> remove(@Parameter(description = "购物车行ID") @PathVariable Long lineId) {
        bookingApplicationService.removeFromBookingCart(currentPatronId(), lineId);
        return APIResponse.ok();
    }

    @PostMapping("/checkout")
    @Operation(summary = "结算购物车", description = "选中的行合并创建一个预约组，成功后清掉选中的行；"
            + "任意一行陪玩+时段冲突则整单失败，不留下部分创建的组")
    public APIResponse<Long> checkout(@Valid @RequestBody CheckoutBookingCartRequest request) {
        Long groupId = bookingApplicationService.checkoutBookingCart(currentPatronId(), request.getLineIds());
        return APIResponse.ok(groupId);
    }

    private Long currentPatronId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
