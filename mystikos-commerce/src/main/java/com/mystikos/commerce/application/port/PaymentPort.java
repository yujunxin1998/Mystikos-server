package com.mystikos.commerce.application.port;

import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;

import java.math.BigDecimal;

/**
 * 出站端口：发起结账支付。MVP 阶段本地注入 mystikos-payment 的
 * PaymentApplicationService，拆微服务时换 Feign/HTTP 客户端，
 * 见 docs/architecture/module-structure.md 的跨模块通信规则。
 *
 * <p>provider/scene 直接复用 mystikos-payment 的枚举而不是在这里重新定义一套镜像类型——
 * 这两个模块本来就单体内直接依赖 mystikos-payment（见 CommercePaymentPortImpl），
 * 重复定义只会变成两边手动同步的负担，不会带来真正的隔离。
 */
public interface PaymentPort {

    PaymentCheckoutResult requestPayment(Long orderId, Long patronId, BigDecimal amount, String currency,
                                          PaymentProvider provider, PaymentScene scene);
}
