package com.mystikos.booking.adapter.web.dto;

import com.mystikos.booking.application.port.PaymentCheckoutResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "支付结账响应")
public class PaymentCheckoutResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付意图ID")
    private Long intentId;

    @Schema(description = "Stripe client secret，前端用它调用 Stripe.js 完成支付")
    private String clientSecret;

    @Schema(description = "支付状态")
    private String status;

    public static PaymentCheckoutResponse from(PaymentCheckoutResult result) {
        PaymentCheckoutResponse dto = new PaymentCheckoutResponse();
        dto.setIntentId(result.intentId());
        dto.setClientSecret(result.clientSecret());
        dto.setStatus(result.status());
        return dto;
    }
}
