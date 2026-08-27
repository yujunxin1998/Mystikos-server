package com.mystikos.payment.adapter.web;

import com.mystikos.payment.application.port.WebhookNotification;
import com.mystikos.payment.application.service.PaymentApplicationService;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.model.PaymentProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付网关的 webhook 回调入口。这是外部系统入站，不是跨限界上下文调用，不走 Port，
 * 也不受 mystikos-common-security 的 JWT 校验保护——安全性完全靠各网关实现里的签名验证
 * （见 {@code PaymentGatewayClient#parseWebhookEvent}）。
 *
 * <p>三家网关的验签方式和"我已收到"回执格式完全不同，不能共用一个响应体：
 * Stripe 认 2xx/非 2xx；支付宝要求纯文本 "success"/"failure"，回别的内容会被当成失败一直重推；
 * 微信 APIv3 要求 JSON {@code {"code":"SUCCESS","message":"..."}}。
 *
 * <p>{@code rawBody} 必须原样接收成 String，不能反序列化成 DTO 再转字符串——Stripe/微信的
 * 签名都是对请求体原始字节算的，任何一次反序列化再序列化都可能因为字段顺序/空白符不同而验签失败。
 */
@RestController
@RequestMapping("/api/v1/payments/webhooks")
@Tag(name = "支付回调", description = "支付网关 webhook，不对外文档化调用方式")
public class PaymentCallbackController {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackController.class);

    private final PaymentApplicationService paymentApplicationService;

    public PaymentCallbackController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping(value = "/stripe", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Stripe webhook", description = "Stripe 控制台配置的回调地址，验签失败返回非 2xx 触发 Stripe 重试")
    public void stripeWebhook(@RequestBody String rawPayload,
                               @RequestHeader("Stripe-Signature") String signatureHeader) {
        paymentApplicationService.handleWebhook(PaymentProvider.STRIPE,
                WebhookNotification.ofRawBody(rawPayload, Map.of("Stripe-Signature", signatureHeader)));
    }

    @PostMapping(value = "/alipay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "支付宝异步通知", description = "支付宝商户后台配置的 notify_url；必须原样返回纯文本 "
            + "success/failure，返回别的内容支付宝会判定失败并按退避策略持续重推")
    public ResponseEntity<String> alipayWebhook(@RequestParam Map<String, String> formParams) {
        try {
            paymentApplicationService.handleWebhook(PaymentProvider.ALIPAY, WebhookNotification.ofFormParams(formParams));
            return ResponseEntity.ok("success");
        } catch (PaymentException e) {
            log.warn("支付宝 webhook 处理失败：{}", e.getMessage());
            return ResponseEntity.ok("failure");
        }
    }

    @PostMapping(value = "/wechat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "微信支付异步通知", description = "微信商户平台配置的回调地址，APIv3 格式；"
            + "验签/解密失败返回非 2xx + code=FAIL 触发微信重推")
    public ResponseEntity<Map<String, String>> wechatWebhook(@RequestBody String rawBody,
                                                               HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Wechatpay-Signature", request.getHeader("Wechatpay-Signature"));
        headers.put("Wechatpay-Timestamp", request.getHeader("Wechatpay-Timestamp"));
        headers.put("Wechatpay-Nonce", request.getHeader("Wechatpay-Nonce"));
        headers.put("Wechatpay-Serial", request.getHeader("Wechatpay-Serial"));

        try {
            paymentApplicationService.handleWebhook(PaymentProvider.WECHAT_PAY,
                    WebhookNotification.ofRawBody(rawBody, headers));
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (PaymentException e) {
            log.warn("微信支付 webhook 处理失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", "FAIL", "message", e.getMessage() == null ? "处理失败" : e.getMessage()));
        }
    }
}
