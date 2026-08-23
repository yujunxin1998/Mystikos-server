package com.mystikos.commerce.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "下单请求")
public class CreateOrderRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收货地址")
    @NotBlank
    private String shippingAddress;
}
