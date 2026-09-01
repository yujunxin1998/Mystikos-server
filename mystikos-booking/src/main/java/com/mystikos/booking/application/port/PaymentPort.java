package com.mystikos.booking.application.port;

import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;

import java.math.BigDecimal;

/**
 * 出站端口：发起结账支付。MVP 阶段由 infrastructure/acl 里的实现本地注入
 * mystikos-payment 的 PaymentApplicationService，拆微服务时换成 Feign/HTTP 客户端，
 * 这里的接口和调用方（BookingApplicationService）都不用改，
 * 见 docs/architecture/module-structure.md 的跨模块通信规则。
 *
 * <p>provider/scene 直接复用 mystikos-payment 的枚举，理由同 Commerce 那份 PaymentPort。
 */
public interface PaymentPort {

    PaymentCheckoutResult requestPayment(Long bookingId, Long patronId, BigDecimal amount, String currency,
                                          PaymentProvider provider, PaymentScene scene);

    /** 同上，但回指的是一个预约组（多条预约合并支付），而不是单条 BookingOrder。 */
    PaymentCheckoutResult requestGroupPayment(Long groupId, Long patronId, BigDecimal amount, String currency,
                                               PaymentProvider provider, PaymentScene scene);
}
