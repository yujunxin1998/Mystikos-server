package com.mystikos.commerce.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "下单请求：用购物车中选中的部分/全部行下单")
public class CreateOrderRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    @Schema(description = "购物车中要结算的商品ID列表，未列入的行继续留在购物车里")
    private List<Long> productIds;

    @NotNull
    @Schema(description = "收货地址ID，引用地址簿")
    private Long addressId;
}
