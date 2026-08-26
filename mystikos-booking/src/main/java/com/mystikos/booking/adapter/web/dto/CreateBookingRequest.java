package com.mystikos.booking.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "创建预约请求；下单老板取自登录态，价格由服务端按陪玩当前时薪 x 时长计算")
public class CreateBookingRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "陪玩用户ID")
    @NotNull
    private Long companionId;

    @Schema(description = "预约开始时间")
    @NotNull
    @Future
    private OffsetDateTime start;

    @Schema(description = "预约时长（小时），最小1小时，最多1位小数")
    @NotNull
    @DecimalMin(value = "1.0", message = "预约时长最少1小时")
    @DecimalMax(value = "24.0", message = "预约时长最多24小时")
    @Digits(integer = 2, fraction = 1)
    private BigDecimal durationHours;
}
