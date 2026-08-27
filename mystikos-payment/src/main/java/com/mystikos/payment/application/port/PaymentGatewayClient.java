package com.mystikos.payment.application.port;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付网关无关接口——PaymentApplicationService 不感知底层具体是 Stripe/支付宝/微信支付。
 * 新增网关时只需要新实现这一个接口（按需实现 {@link PayoutGatewayClient}），不改用例代码。
 *
 * <p>陪玩提现打款（Stripe Connect Express）跟这里的收款下单是两码事，且支付宝/微信没有
 * 对等的打款能力，拆到 {@link PayoutGatewayClient} 单独一个接口，避免这里的实现被迫塞一堆
 * {@code UnsupportedOperationException}。
 */
public interface PaymentGatewayClient {

    /** 网关标识，落在 PaymentIntent.gatewayProvider 上，如 "stripe"/"alipay"/"wechat_pay"。 */
    String providerCode();

    /**
     * 建一笔网关侧的支付意图。idempotencyKey 会原样透传给网关自己的幂等机制
     * （如 Stripe 的 Idempotency-Key 请求头），双重幂等：我们自己按 (sourceType, sourceId)
     * 复用未终态 intent，网关那边也不会因为我们的网络重试而重复扣款。
     *
     * <p>scene 决定调用网关的哪个具体产品接口，返回结果的 payloadType 也会跟着变，
     * 见 {@link PaymentScene}/{@link PaymentPayloadType}。不支持 CNY 以外币种的网关
     * （目前是支付宝/微信）应该在这里直接拒绝，而不是静默按错误汇率换算。
     */
    GatewayIntentResult createIntent(String idempotencyKey, BigDecimal amount, String currency,
                                      Map<String, String> metadata, PaymentScene scene);

    /**
     * 解析并验签 webhook 回调。签名校验失败必须抛 {@link com.mystikos.payment.domain.PaymentException}，
     * 不能返回一个"忽略"结果糊弄过去——伪造的 webhook 是能白嫖发货/发礼物的真实攻击面。
     */
    GatewayWebhookEvent parseWebhookEvent(WebhookNotification notification);

    void refund(String gatewayRef, BigDecimal amount);
}
