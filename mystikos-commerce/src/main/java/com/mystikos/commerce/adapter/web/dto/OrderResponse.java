package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.application.service.OrderView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Schema(description = "订单视图")
public class OrderResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "老板用户ID")
    private Long patronId;

    @Schema(description = "订单行")
    private List<OrderLineItemResponse> items;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "收货地址")
    private String shippingAddress;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "支付有效期截止时间；DRAFT/PENDING_PAYMENT 状态下过了这个时间会被判定为 EXPIRED")
    private OffsetDateTime expiresAt;

    public static OrderResponse from(OrderView view) {
        OrderResponse dto = new OrderResponse();
        dto.setOrderId(view.orderId());
        dto.setPatronId(view.patronId());
        dto.setItems(view.items().stream().map(OrderLineItemResponse::from).toList());
        dto.setTotalAmount(view.totalAmount());
        dto.setShippingAddress(view.shippingAddress());
        dto.setStatus(view.status().name());
        dto.setCreatedAt(view.createdAt());
        dto.setExpiresAt(view.expiresAt());
        return dto;
    }
}
