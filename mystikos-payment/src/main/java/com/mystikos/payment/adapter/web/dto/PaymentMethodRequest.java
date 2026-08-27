package com.mystikos.payment.adapter.web.dto;

import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 结账/充值接口共用的支付方式选择请求体。commerce/booking 各自的结账接口和这里的
 * 钱包充值接口都直接复用这一个类型，不各自重复定义。
 */
@Data
@Schema(description = "支付方式选择")
public class PaymentMethodRequest {

    @NotNull
    @Schema(description = "支付渠道")
    private PaymentProvider provider;

    @Schema(description = "下单场景：PC 扫码/手机 H5/App 调起；Stripe 忽略这个字段，不传时默认 DEFAULT")
    private PaymentScene scene = PaymentScene.DEFAULT;
}
