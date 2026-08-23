package com.mystikos.payment.application.port;

/**
 * 网关无关的事件语义。具体网关（如 Stripe 的 payment_intent.succeeded/payment_intent.payment_failed/
 * charge.refunded）在自己的实现里把网关事件类型映射到这几种，PaymentApplicationService 不感知网关原始事件名。
 */
public enum GatewayEventType {
    CAPTURED,
    FAILED,
    REFUNDED,
    /** 我们不关心的事件类型（如账户资料更新），直接忽略。 */
    IGNORED
}
