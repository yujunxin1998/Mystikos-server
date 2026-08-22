package com.mystikos.booking.adapter.web;

import com.mystikos.booking.adapter.web.dto.CreateBookingRequest;
import com.mystikos.booking.application.command.CreateBookingCommand;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.common.result.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
}
