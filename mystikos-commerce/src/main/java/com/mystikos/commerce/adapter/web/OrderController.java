package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.CreateOrderRequest;
import com.mystikos.commerce.adapter.web.dto.OrderResponse;
import com.mystikos.commerce.application.command.CreateOrderCommand;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.common.result.APIResponse;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 下单/取消，尚未接支付——和 mystikos-booking 的 BookingController 是同一个阶段，
 * 订单创建后停在 DRAFT，PENDING_PAYMENT/PAID 之后的流转方法先留在聚合上不接用例。
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
    @Operation(summary = "下单", description = "用当前购物车内容下单，成功后清空购物车；任意一行库存不足则整单失败")
    public APIResponse<Long> create(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = commerceApplicationService.createOrder(new CreateOrderCommand(
                currentPatronId(), request.getShippingAddress()));
        return APIResponse.ok(orderId);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "订单详情")
    public APIResponse<OrderResponse> get(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.getOrder(orderId)));
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
