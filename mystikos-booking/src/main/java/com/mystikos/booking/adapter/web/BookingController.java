package com.mystikos.booking.adapter.web;

import com.mystikos.booking.adapter.web.dto.CreateBookingRequest;
import com.mystikos.booking.adapter.web.dto.PaymentCheckoutResponse;
import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.common.result.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "预约撮合", description = "陪玩预约下单")
public class BookingController {

    private final BookingApplicationService bookingApplicationService;

    public BookingController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @PostMapping
    @Operation(summary = "创建预约", description = "创建一笔 DRAFT 状态的预约订单，尚未接支付/陪玩定价校验")
    public APIResponse<Long> create(@Valid @RequestBody CreateBookingRequest request) {
        Long bookingId = bookingApplicationService.createBooking(new CreateBookingCommand(
                request.getPatronId(),
                request.getCompanionId(),
                request.getSkuId(),
                request.getStart(),
                request.getEnd(),
                request.getPriceSnapshot()));
        return APIResponse.ok(bookingId);
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "发起结账", description = "把预约订单转 PENDING_PAYMENT，返回前端用 Stripe.js 完成支付所需的 clientSecret")
    public APIResponse<PaymentCheckoutResponse> requestPayment(@Parameter(description = "预约订单ID") @PathVariable Long id) {
        return APIResponse.ok(PaymentCheckoutResponse.from(bookingApplicationService.requestPayment(id)));
    }
}
