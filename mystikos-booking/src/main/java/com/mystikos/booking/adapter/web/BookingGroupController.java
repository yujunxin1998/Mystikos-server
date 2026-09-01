package com.mystikos.booking.adapter.web;

import com.mystikos.booking.adapter.web.dto.PaymentCheckoutResponse;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.booking.application.service.BookingOrderGroupView;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.payment.adapter.web.dto.PaymentMethodRequest;
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
import org.springframework.web.bind.annotation.RestController;

/** 预约组：多条预约合并支付后的载体，一次支付覆盖组内所有预约，见 BookingOrderGroup 类注释。 */
@RestController
@RequestMapping("/api/v1/booking-groups")
@Tag(name = "预约撮合", description = "预约组（合并支付）")
@SecurityRequirement(name = "bearerAuth")
public class BookingGroupController {

    private final BookingApplicationService bookingApplicationService;

    public BookingGroupController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "预约组详情", description = "只能查自己的组；查询时会先懒同步过期状态，expiresAt 供前端倒计时")
    public APIResponse<BookingOrderGroupView> get(@Parameter(description = "预约组ID") @PathVariable Long groupId) {
        return APIResponse.ok(bookingApplicationService.getBookingGroup(groupId, currentPatronId()));
    }

    @PostMapping("/{groupId}/payment")
    @Operation(summary = "发起组合支付", description = "把预约组转 PENDING_PAYMENT 并级联到组内所有预约，返回前端完成支付所需的 payload；"
            + "组已失效时返回错误；选支付宝/微信时注意结算币种固定欧元，非 CNY 网关会直接拒绝")
    public APIResponse<PaymentCheckoutResponse> requestPayment(@Parameter(description = "预约组ID") @PathVariable Long groupId,
                                                                 @Valid @RequestBody PaymentMethodRequest request) {
        return APIResponse.ok(PaymentCheckoutResponse.from(bookingApplicationService.requestGroupPayment(
                groupId, currentPatronId(), request.getProvider(), request.getScene())));
    }

    @PostMapping("/{groupId}/cancel")
    @Operation(summary = "取消预约组", description = "只允许 DRAFT/PENDING_PAYMENT 状态；PAID 之后请对组内单条预约分别操作")
    public APIResponse<Void> cancel(@Parameter(description = "预约组ID") @PathVariable Long groupId) {
        bookingApplicationService.cancelGroup(groupId, currentPatronId());
        return APIResponse.ok();
    }

    private Long currentPatronId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
