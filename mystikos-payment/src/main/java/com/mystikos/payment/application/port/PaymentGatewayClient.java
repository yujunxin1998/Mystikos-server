package com.mystikos.payment.application.port;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付网关无关接口——PaymentApplicationService 只依赖这个接口，不感知底层是 Stripe
 * 还是以后要加的 Adyen/Airwallex。新增网关时只需要新实现这一个接口，不改用例代码。
 */
public interface PaymentGatewayClient {

    /** 网关标识，落在 PaymentIntent.gatewayProvider 上，如 "stripe"。 */
    String providerCode();

    /**
     * 建一笔网关侧的支付意图。idempotencyKey 会原样透传给网关自己的幂等机制
     * （如 Stripe 的 Idempotency-Key 请求头），双重幂等：我们自己按 (sourceType, sourceId)
     * 复用未终态 intent，网关那边也不会因为我们的网络重试而重复扣款。
     */
    GatewayIntentResult createIntent(String idempotencyKey, BigDecimal amount, String currency,
                                      Map<String, String> metadata);

    /**
     * 解析并验签 webhook 回调。签名校验失败必须抛 {@link com.mystikos.payment.domain.PaymentException}，
     * 不能返回一个"忽略"结果糊弄过去——伪造的 webhook 是能白嫖发货/发礼物的真实攻击面。
     */
    GatewayWebhookEvent parseWebhookEvent(String rawPayload, String signatureHeader);

    void refund(String gatewayRef, BigDecimal amount);

    /** 创建一个 Stripe Connect Express 账户，返回网关侧账户 id。 */
    String createConnectAccount(String email);

    /** 生成 Connect 账户完成入驻资料所需的一次性跳转链接。 */
    String createConnectOnboardingLink(String connectAccountId, String returnUrl, String refreshUrl);

    /** 实时查询这个 Connect 账户是否已经通过 Stripe 审核、可以接收打款——不缓存，每次都问网关。 */
    boolean isPayoutReady(String connectAccountId);

    /** 从平台余额打款给某个已完成入驻的 Connect 账户，返回网关侧转账 id。 */
    String transferToConnectAccount(String connectAccountId, BigDecimal amount, String currency, String idempotencyKey);
}
