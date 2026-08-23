package com.mystikos.payment.adapter.web;

import com.mystikos.payment.application.service.PaymentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付网关的 webhook 回调入口。这是外部系统入站，不是跨限界上下文调用，不走 Port，
 * 也不受 mystikos-common-security 的 JWT 校验保护——安全性完全靠
 * {@link PaymentApplicationService#handleStripeWebhook} 内部的签名验证。
 *
 * <p>{@code rawPayload} 必须原样接收成 String，不能反序列化成 DTO 再转字符串
 * ——Stripe 的签名是对请求体原始字节算的，任何一次反序列化再序列化都可能因为
 * 字段顺序/空白符不同而验签失败。
 */
@RestController
@RequestMapping("/api/v1/payments/webhooks")
@Tag(name = "支付回调", description = "支付网关 webhook，不对外文档化调用方式")
public class PaymentCallbackController {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentCallbackController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping(value = "/stripe", consumes = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.OK)
    @Operation(summary = "Stripe webhook", description = "Stripe 控制台配置的回调地址，验签失败返回非 2xx 触发 Stripe 重试")
    public void stripeWebhook(@RequestBody String rawPayload,
                               @RequestHeader("Stripe-Signature") String signatureHeader) {
        paymentApplicationService.handleStripeWebhook(rawPayload, signatureHeader);
    }
}
