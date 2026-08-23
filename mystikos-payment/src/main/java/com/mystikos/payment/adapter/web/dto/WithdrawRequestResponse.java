package com.mystikos.payment.adapter.web.dto;

import com.mystikos.payment.domain.model.WithdrawRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "提现申请视图")
public class WithdrawRequestResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "申请时间")
    private OffsetDateTime requestedAt;

    public static WithdrawRequestResponse from(WithdrawRequest request) {
        WithdrawRequestResponse dto = new WithdrawRequestResponse();
        dto.setId(request.getId());
        dto.setAmount(request.getAmount());
        dto.setCurrency(request.getCurrency());
        dto.setStatus(request.getStatus().name());
        dto.setRejectReason(request.getRejectReason());
        dto.setRequestedAt(request.getRequestedAt());
        return dto;
    }
}
