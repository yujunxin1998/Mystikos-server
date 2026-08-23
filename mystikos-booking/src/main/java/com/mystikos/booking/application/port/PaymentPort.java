package com.mystikos.booking.application.port;

import java.math.BigDecimal;

/**
 * 出站端口：发起结账支付。MVP 阶段由 infrastructure/acl 里的实现本地注入
 * mystikos-payment 的 PaymentApplicationService，拆微服务时换成 Feign/HTTP 客户端，
 * 这里的接口和调用方（BookingApplicationService）都不用改，
 * 见 docs/architecture/module-structure.md 的跨模块通信规则。
 */
public interface PaymentPort {

    PaymentCheckoutResult requestPayment(Long bookingId, Long patronId, BigDecimal amount, String currency);
}
