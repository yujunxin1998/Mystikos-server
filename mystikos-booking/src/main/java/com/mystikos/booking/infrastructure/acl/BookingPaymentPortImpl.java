package com.mystikos.booking.infrastructure.acl;

import com.mystikos.booking.application.port.PaymentCheckoutResult;
import com.mystikos.booking.application.port.PaymentPort;
import com.mystikos.payment.application.command.CreatePaymentIntentCommand;
import com.mystikos.payment.application.service.PaymentApplicationService;
import com.mystikos.payment.application.service.PaymentIntentResult;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 防腐层：把 mystikos-payment 的 PaymentApplicationService 翻译成本模块自己的
 * PaymentPort/PaymentCheckoutResult，本模块只依赖这两个自己定义的类型，
 * 不把 Payment 的返回类型直接透出到 BookingApplicationService 之外。
 * MVP 阶段是本地 Bean 注入，拆微服务时换 Feign 客户端。
 *
 * <p>类名带 Booking 前缀——{@code MystikosApplication} 用
 * {@code @SpringBootApplication(scanBasePackages = "com.mystikos")} 把所有模块
 * 当一个扁平命名空间扫描，Commerce/Gifting 也各有一个同职责的 PaymentPortImpl，
 * 简单类名相同会在启动时报 ConflictingBeanDefinitionException。
 */
@Component
public class BookingPaymentPortImpl implements PaymentPort {

    private final PaymentApplicationService paymentApplicationService;

    public BookingPaymentPortImpl(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @Override
    public PaymentCheckoutResult requestPayment(Long bookingId, Long patronId,
                                                 BigDecimal amount, String currency) {
        PaymentIntentResult result = paymentApplicationService.createIntent(
                new CreatePaymentIntentCommand(SourceType.BOOKING, bookingId, patronId, amount, currency));
        return new PaymentCheckoutResult(result.intentId(), result.clientSecret(), result.status().name());
    }
}
