package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.domain.model.OrderLineItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "订单行视图")
public class OrderLineItemResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "下单时的商品名快照")
    private String productNameSnapshot;

    @Schema(description = "下单时的单价快照")
    private BigDecimal unitPriceSnapshot;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    public static OrderLineItemResponse from(OrderLineItem item) {
        OrderLineItemResponse dto = new OrderLineItemResponse();
        dto.setProductId(item.productId());
        dto.setProductNameSnapshot(item.productNameSnapshot());
        dto.setUnitPriceSnapshot(item.unitPriceSnapshot());
        dto.setQuantity(item.quantity());
        dto.setSubtotal(item.subtotal());
        return dto;
    }
}
