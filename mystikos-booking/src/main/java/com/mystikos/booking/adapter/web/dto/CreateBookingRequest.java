package com.mystikos.booking.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "创建预约请求")
public class CreateBookingRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "下单老板用户ID")
    @NotNull
    private Long patronId;

    @Schema(description = "陪玩用户ID")
    @NotNull
    private Long companionId;

    @Schema(description = "服务SKU ID")
    @NotNull
    private Long skuId;

    @Schema(description = "预约时段开始时间")
    @NotNull
    @Future
    private OffsetDateTime start;

    @Schema(description = "预约时段结束时间")
    @NotNull
    @Future
    private OffsetDateTime end;

    @Schema(description = "下单时的价格快照")
    @NotNull
    private BigDecimal priceSnapshot;
}
