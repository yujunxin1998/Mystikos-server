package com.mystikos.payment.adapter.web.dto;

import com.mystikos.payment.application.service.PaymentIntentResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "支付结账响应，前端按 payloadType 决定怎么消费 payload：CLIENT_SECRET 调 Stripe.js，"
        + "REDIRECT_URL 跳转，QR_CODE 渲染二维码，APP_INVOKE_PARAMS 原样传给 App 内支付宝/微信 SDK")
public class PaymentCheckoutResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付意图ID")
    private Long intentId;

    @Schema(description = "结果类型：CLIENT_SECRET/REDIRECT_URL/QR_CODE/APP_INVOKE_PARAMS")
    private String payloadType;

    @Schema(description = "结果内容，key 随 payloadType 而定")
    private Map<String, String> payload;

    @Schema(description = "支付状态")
    private String status;

    public static PaymentCheckoutResponse from(PaymentIntentResult result) {
        PaymentCheckoutResponse dto = new PaymentCheckoutResponse();
        dto.setIntentId(result.intentId());
        dto.setPayloadType(result.payloadType() == null ? null : result.payloadType().name());
        dto.setPayload(result.payload());
        dto.setStatus(result.status().name());
        return dto;
    }
}
