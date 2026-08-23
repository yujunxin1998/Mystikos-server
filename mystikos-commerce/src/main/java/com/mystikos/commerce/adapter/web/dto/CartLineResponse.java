package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.application.service.CartLineView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "购物车行视图")
public class CartLineResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名")
    private String productName;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    public static CartLineResponse from(CartLineView view) {
        CartLineResponse dto = new CartLineResponse();
        dto.setProductId(view.productId());
        dto.setProductName(view.productName());
        dto.setUnitPrice(view.unitPrice());
        dto.setQuantity(view.quantity());
        dto.setSubtotal(view.subtotal());
        return dto;
    }
}
