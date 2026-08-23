package com.mystikos.payment.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "钱包充值请求")
public class RechargeRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "充值金额必须大于 0")
    @Schema(description = "充值金额")
    private BigDecimal amount;

    @NotBlank
    @Schema(description = "ISO 4217 币种代码，如 EUR")
    private String currency;
}
