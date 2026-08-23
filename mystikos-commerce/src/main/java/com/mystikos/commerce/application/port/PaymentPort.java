package com.mystikos.commerce.application.port;

import java.math.BigDecimal;

/**
 * 出站端口：发起结账支付。MVP 阶段本地注入 mystikos-payment 的
 * PaymentApplicationService，拆微服务时换 Feign/HTTP 客户端，
 * 见 docs/architecture/module-structure.md 的跨模块通信规则。
 */
public interface PaymentPort {

    PaymentCheckoutResult requestPayment(Long orderId, Long patronId, BigDecimal amount, String currency);
}
