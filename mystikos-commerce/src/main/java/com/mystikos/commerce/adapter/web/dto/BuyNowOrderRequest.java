package com.mystikos.commerce.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "立即购买请求：跳过购物车，直接用商品+数量下单")
public class BuyNowOrderRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(description = "商品ID")
    private Long productId;

    @Min(1)
    @Schema(description = "购买数量")
    private int quantity = 1;

    @NotNull
    @Schema(description = "收货地址ID，引用地址簿")
    private Long addressId;
}
