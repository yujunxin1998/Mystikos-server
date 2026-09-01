package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.BuyNowOrderRequest;
import com.mystikos.commerce.adapter.web.dto.CreateOrderRequest;
import com.mystikos.commerce.adapter.web.dto.OrderResponse;
import com.mystikos.commerce.adapter.web.dto.PaymentCheckoutResponse;
import com.mystikos.commerce.application.command.CreateDirectOrderCommand;
import com.mystikos.commerce.application.command.CreateOrderCommand;
import com.mystikos.commerce.application.service.CommerceApplicationService;
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

/**
 * C 端下单/查询/取消。PAID 之后的发货流转、后台取消/退款由
 * {@link OrderAdminController} 驱动，不在这里开放给买家自己操作。
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "商城", description = "商城订单")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CommerceApplicationService commerceApplicationService;

    public OrderController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @PostMapping
    @Operation(summary = "下单", description = "用购物车中选中的部分/全部行下单，成功后只清掉选中的行；任意一行库存不足则整单失败")
    public APIResponse<Long> create(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = commerceApplicationService.createOrder(new CreateOrderCommand(
                currentPatronId(), request.getProductIds(), request.getAddressId()));
        return APIResponse.ok(orderId);
    }

    @PostMapping("/buy-now")
    @Operation(summary = "立即购买", description = "跳过购物车，直接用商品+数量下单，不影响购物车里已有的行")
    public APIResponse<Long> buyNow(@Valid @RequestBody BuyNowOrderRequest request) {
        Long orderId = commerceApplicationService.createDirectOrder(new CreateDirectOrderCommand(
                currentPatronId(), request.getProductId(), request.getQuantity(), request.getAddressId()));
        return APIResponse.ok(orderId);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "订单详情")
    public APIResponse<OrderResponse> get(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.getOrder(orderId, currentPatronId())));
    }

    @PostMapping("/{orderId}/payment")
    @Operation(summary = "发起结账", description = "把订单转 PENDING_PAYMENT，返回前端完成支付所需的 payload；"
            + "选支付宝/微信时注意结算币种固定欧元，非 CNY 网关会直接拒绝")
    public APIResponse<PaymentCheckoutResponse> requestPayment(@Parameter(description = "订单ID") @PathVariable Long orderId,
                                                                @Valid @RequestBody PaymentMethodRequest request) {
        return APIResponse.ok(PaymentCheckoutResponse.from(
                commerceApplicationService.requestPayment(orderId, currentPatronId(), request.getProvider(), request.getScene())));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "只有本人的订单能取消；DRAFT/PENDING_PAYMENT/PAID 状态才允许，取消后释放预占库存")
    public APIResponse<Void> cancel(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        commerceApplicationService.cancelOrder(currentPatronId(), orderId);
        return APIResponse.ok();
    }

    private Long currentPatronId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
