package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.OrderResponse;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.commerce.application.service.OrderView;
import com.mystikos.commerce.domain.model.OrderStatus;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口：订单查询与状态流转。归入 {@code /api/v1/manage/**} 路由前缀，不限订单所属买家。
 * 订单只能由买家下单产生，后台不提供手动创建；"删除"用取消/退款代替，不做物理删除。
 * C 端订单接口见 {@link OrderController}。
 */
@RestController
@RequestMapping("/api/v1/manage/orders")
@Tag(name = "后台管理 - 订单管理", description = "订单查询、发货流转、取消与退款")
@SecurityRequirement(name = "bearerAuth")
public class OrderAdminController {

    private final CommerceApplicationService commerceApplicationService;

    public OrderAdminController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @GetMapping
    @Operation(summary = "分页查询订单", description = "不限买家，可按状态/买家ID过滤；均不传则返回全部")
    public APIResponse<PageResult<OrderResponse>> list(
            @Parameter(description = "订单状态，不传则不限") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "买家用户ID，不传则不限") @RequestParam(required = false) Long patronId,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<OrderView> page = commerceApplicationService.listOrdersForAdmin(status, patronId, pageNum, pageSize);
        List<OrderResponse> records = page.records().stream().map(OrderResponse::from).toList();
        return APIResponse.ok(PageResult.of(records, page.total(), page.pageNum(), page.pageSize()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "订单详情（后台）", description = "不限买家")
    public APIResponse<OrderResponse> get(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.getOrder(orderId)));
    }

    @PutMapping("/{orderId}/start-fulfilling")
    @Operation(summary = "开始处理订单", description = "PAID → FULFILLING")
    public APIResponse<OrderResponse> startFulfilling(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.startFulfillingOrder(orderId)));
    }

    @PutMapping("/{orderId}/ship")
    @Operation(summary = "订单发货", description = "FULFILLING → SHIPPED")
    public APIResponse<OrderResponse> ship(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.shipOrder(orderId)));
    }

    @PutMapping("/{orderId}/complete")
    @Operation(summary = "完成订单", description = "SHIPPED → COMPLETED")
    public APIResponse<OrderResponse> complete(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.completeOrder(orderId)));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "不限买家本人；DRAFT/PENDING_PAYMENT/PAID 状态才允许，取消后释放预占库存")
    public APIResponse<OrderResponse> cancel(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.adminCancelOrder(orderId)));
    }

    @PutMapping("/{orderId}/refund")
    @Operation(summary = "订单退款", description = "PAID/FULFILLING/SHIPPED/COMPLETED 状态才允许，退款后释放预占库存")
    public APIResponse<OrderResponse> refund(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return APIResponse.ok(OrderResponse.from(commerceApplicationService.refundOrder(orderId)));
    }
}
