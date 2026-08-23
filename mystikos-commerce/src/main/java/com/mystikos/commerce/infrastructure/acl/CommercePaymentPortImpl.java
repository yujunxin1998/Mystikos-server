package com.mystikos.commerce.infrastructure.acl;

import com.mystikos.commerce.application.port.PaymentCheckoutResult;
import com.mystikos.commerce.application.port.PaymentPort;
import com.mystikos.payment.application.command.CreatePaymentIntentCommand;
import com.mystikos.payment.application.service.PaymentApplicationService;
import com.mystikos.payment.application.service.PaymentIntentResult;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 防腐层：把 mystikos-payment 的 PaymentApplicationService 翻译成本模块自己的
 * PaymentPort/PaymentCheckoutResult。MVP 阶段是本地 Bean 注入，拆微服务时换 Feign。
 *
 * <p>类名带 Commerce 前缀——全模块扁平扫描下不能和 Booking/Gifting 各自的
 * 同职责 PaymentPortImpl 简单类名重复，否则启动时报 ConflictingBeanDefinitionException。
 */
@Component
public class CommercePaymentPortImpl implements PaymentPort {

    private final PaymentApplicationService paymentApplicationService;

    public CommercePaymentPortImpl(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @Override
    public PaymentCheckoutResult requestPayment(Long orderId, Long patronId, BigDecimal amount, String currency) {
        PaymentIntentResult result = paymentApplicationService.createIntent(
                new CreatePaymentIntentCommand(SourceType.MERCHANDISE, orderId, patronId, amount, currency));
        return new PaymentCheckoutResult(result.intentId(), result.clientSecret(), result.status().name());
    }
}
