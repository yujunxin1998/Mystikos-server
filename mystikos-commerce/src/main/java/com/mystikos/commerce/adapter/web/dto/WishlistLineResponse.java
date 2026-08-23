package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.application.service.WishlistLineView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "心愿单行视图")
public class WishlistLineResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名")
    private String productName;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "加入时间")
    private OffsetDateTime addedAt;

    public static WishlistLineResponse from(WishlistLineView view) {
        WishlistLineResponse dto = new WishlistLineResponse();
        dto.setProductId(view.productId());
        dto.setProductName(view.productName());
        dto.setUnitPrice(view.unitPrice());
        dto.setAddedAt(view.addedAt());
        return dto;
    }
}
