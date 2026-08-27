package com.mystikos.payment.application.port;

import java.util.Map;

/**
 * 三家网关的回调验签方式完全不同，用这一个通用载体传给
 * {@link PaymentGatewayClient#parseWebhookEvent}：Stripe/微信 APIv3 靠请求头
 * （Stripe-Signature / Wechatpay-Signature 等）+ 原始 body 验签；支付宝的签名字段
 * 本身就在表单参数里，没有独立的签名头。各网关实现只取自己需要的那部分。
 */
public record WebhookNotification(String rawBody, Map<String, String> headers, Map<String, String> formParams) {

    public static WebhookNotification ofRawBody(String rawBody, Map<String, String> headers) {
        return new WebhookNotification(rawBody, headers, Map.of());
    }

    public static WebhookNotification ofFormParams(Map<String, String> formParams) {
        return new WebhookNotification(null, Map.of(), formParams);
    }
}
