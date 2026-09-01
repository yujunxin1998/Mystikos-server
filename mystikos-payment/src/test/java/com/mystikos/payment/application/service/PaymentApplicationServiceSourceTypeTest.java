package com.mystikos.payment.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.payment.application.command.CreatePaymentIntentCommand;
import com.mystikos.payment.application.port.GatewayIntentResult;
import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.application.port.PaymentPayloadType;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.PaymentProvider;
import com.mystikos.payment.domain.model.PaymentStatus;
import com.mystikos.payment.domain.model.SourceType;
import com.mystikos.payment.domain.repository.LedgerEntryRepository;
import com.mystikos.payment.domain.repository.PaymentIntentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 纯 Mockito 单元测试——确认新加的 SourceType.BOOKING_GROUP（陪玩预约组合支付）跟其他既有
 * SourceType 一样，直接流过 createIntent，不需要 PaymentApplicationService 做任何特判。
 * 这不是测某个具体行为，而是用测试固化"新增 SourceType 不用碰这个类"这件事本身。
 */
class PaymentApplicationServiceSourceTypeTest {

    private PaymentIntentRepository paymentIntentRepository;
    private PaymentGatewayRegistry gatewayRegistry;
    private PaymentGatewayClient gatewayClient;
    private PaymentApplicationService service;

    @BeforeEach
    void setUp() {
        paymentIntentRepository = mock(PaymentIntentRepository.class);
        LedgerEntryRepository ledgerEntryRepository = mock(LedgerEntryRepository.class);
        gatewayRegistry = mock(PaymentGatewayRegistry.class);
        gatewayClient = mock(PaymentGatewayClient.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);

        service = new PaymentApplicationService(paymentIntentRepository, ledgerEntryRepository, gatewayRegistry,
                eventPublisher);

        when(paymentIntentRepository.findActiveBySource(any(), any())).thenReturn(Optional.empty());
        when(gatewayRegistry.get(PaymentProvider.ALIPAY)).thenReturn(gatewayClient);
        when(gatewayClient.providerCode()).thenReturn("alipay");
        when(gatewayClient.createIntent(any(), any(), any(), any(), any())).thenReturn(
                GatewayIntentResult.qrCode("gateway-ref-1", "weixin://wxpay/qr/x"));

        AtomicLong idSequence = new AtomicLong(1);
        when(paymentIntentRepository.save(any())).thenAnswer(invocation -> {
            PaymentIntent intent = invocation.getArgument(0, PaymentIntent.class);
            if (intent.getId() == null) {
                intent.assignId(idSequence.getAndIncrement());
            }
            return intent;
        });
    }

    @ParameterizedTest
    @EnumSource(SourceType.class)
    void createIntentWorksUniformlyForEverySourceTypeIncludingBookingGroup(SourceType sourceType) {
        CreatePaymentIntentCommand command = new CreatePaymentIntentCommand(sourceType, 42L, 1001L,
                new BigDecimal("100.00"), "CNY", PaymentProvider.ALIPAY, PaymentScene.PC_QR);

        PaymentIntentResult result = service.createIntent(command);

        assertThat(result.payloadType()).isEqualTo(PaymentPayloadType.QR_CODE);
        assertThat(result.status()).isEqualTo(PaymentStatus.REQUIRES_ACTION);
    }
}
