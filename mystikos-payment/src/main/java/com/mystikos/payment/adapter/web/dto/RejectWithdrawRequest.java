package com.mystikos.payment.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "驳回提现申请请求")
public class RejectWithdrawRequest {

    @NotBlank
    @Schema(description = "驳回原因")
    private String reason;
}
