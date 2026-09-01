package com.mystikos.booking.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "结算预约购物车请求：选中的行合并成一个预约组、一次支付")
public class CheckoutBookingCartRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    @Schema(description = "购物车中要结算的行ID列表")
    private List<Long> lineIds;
}
