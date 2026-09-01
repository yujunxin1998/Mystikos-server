package com.mystikos.payment.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.payment.application.command.CreatePaymentIntentCommand;
import com.mystikos.payment.application.port.GatewayEventType;
import com.mystikos.payment.application.port.GatewayIntentResult;
import com.mystikos.payment.application.port.GatewayWebhookEvent;
import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.application.port.WebhookNotification;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.event.PaymentCapturedEvent;
import com.mystikos.payment.domain.event.PaymentRefundedEvent;
import com.mystikos.payment.domain.model.LedgerDirection;
import com.mystikos.payment.domain.model.LedgerEntry;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.PaymentProvider;
import com.mystikos.payment.domain.model.PaymentStatus;
import com.mystikos.payment.domain.repository.LedgerEntryRepository;
import com.mystikos.payment.domain.repository.PaymentIntentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * 走外部网关的支付用例：建单（Booking/Commerce 结账、钱包充值）、webhook 回调、退款。
 * 礼物打赏走钱包内部扣款，不经这里，见 {@link WalletApplicationService#debitForGift}。
 */
@Service
public class PaymentApplicationService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final DomainEventPublisher eventPublisher;

    public PaymentApplicationService(PaymentIntentRepository paymentIntentRepository,
                                      LedgerEntryRepository ledgerEntryRepository,
                                      PaymentGatewayRegistry gatewayRegistry,
                                      DomainEventPublisher eventPublisher) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.gatewayRegistry = gatewayRegistry;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 按 (sourceType, sourceId) 先找是否已有未终态的意图并直接复用——同一笔订单被
     * 重复点击"去支付"不会在网关那边建出两笔单子，调用方不需要自己传幂等键。
     *
     * <p>复用已有意图时不会重新调用网关——如果调用方这次换了 provider/scene（比如上次选支付宝
     * 扫码这次想换微信 H5），会拿到第一次建单时的旧 provider 结果，调用方要自己决定要不要
     * 先把旧意图作废。这次不做"换渠道重新建单"，按现有场景（同一订单结账一般不会中途换渠道）够用。
     */
    @Transactional
    public PaymentIntentResult createIntent(CreatePaymentIntentCommand command) {
        var existing = paymentIntentRepository.findActiveBySource(command.sourceType(), command.sourceId());
        if (existing.isPresent()) {
            return PaymentIntentResult.from(existing.get());
        }

        PaymentGatewayClient gatewayClient = gatewayRegistry.get(command.provider());

        PaymentIntent intent = PaymentIntent.createPending(command.sourceType(), command.sourceId(),
                command.patronId(), command.amount(), command.currency(), UUID.randomUUID().toString());
        PaymentIntent saved = paymentIntentRepository.save(intent);

        GatewayIntentResult gatewayResult = gatewayClient.createIntent(
                saved.getIdempotencyKey(), saved.getAmount(), saved.getCurrency(),
                Map.of(
                        "sourceType", saved.getSourceType().name(),
                        "sourceId", String.valueOf(saved.getSourceId()),
                        "intentId", String.valueOf(saved.getId())),
                command.scene());

        saved.markRequiresAction(gatewayClient.providerCode(), gatewayResult.gatewayRef(),
                gatewayResult.payloadType(), gatewayResult.payload());
        PaymentIntent updated = paymentIntentRepository.save(saved);
        return PaymentIntentResult.from(updated);
    }

    @Transactional
    public void handleWebhook(PaymentProvider provider, WebhookNotification notification) {
        PaymentGatewayClient gatewayClient = gatewayRegistry.get(provider);
        GatewayWebhookEvent event = gatewayClient.parseWebhookEvent(notification);
        if (event.type() == GatewayEventType.IGNORED) {
            return;
        }
        var found = paymentIntentRepository.findByGatewayRef(event.gatewayRef());
        if (found.isEmpty()) {
            // 网关侧发生但和我们无关/尚未落库的事件（如 Dashboard 手工测试单），不是错误，直接忽略。
            return;
        }
        PaymentIntent intent = found.get();
        switch (event.type()) {
            case CAPTURED -> capture(intent);
            case FAILED -> {
                if (intent.getStatus() == PaymentStatus.CAPTURED) {
                    return;
                }
                intent.markFailed(event.failureReason());
                paymentIntentRepository.save(intent);
            }
            case REFUNDED -> {
                if (intent.getStatus() == PaymentStatus.REFUNDED) {
                    return;
                }
                intent.markRefunded();
                paymentIntentRepository.save(intent);
                ledgerEntryRepository.save(LedgerEntry.record(intent.getId(), null,
                        LedgerDirection.DEBIT, intent.getAmount(), intent.getCurrency()));
                eventPublisher.publish(new PaymentRefundedEvent(intent.getId(), intent.getSourceType(),
                        intent.getSourceId(), intent.getPatronId(), intent.getAmount(), intent.getCurrency()));
            }
            default -> { /* IGNORED 已经在上面短路 */ }
        }
    }

    @Transactional
    public void refund(Long intentId, String reason) {
        PaymentIntent intent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> PaymentException.notFound(intentId));
        PaymentGatewayClient gatewayClient = gatewayRegistry.get(providerOf(intent));
        gatewayClient.refund(intent.getGatewayRef(), intent.getAmount());
        intent.markRefunded();
        paymentIntentRepository.save(intent);
        ledgerEntryRepository.save(LedgerEntry.record(intent.getId(), null,
                LedgerDirection.DEBIT, intent.getAmount(), intent.getCurrency()));
        eventPublisher.publish(new PaymentRefundedEvent(intent.getId(), intent.getSourceType(),
                intent.getSourceId(), intent.getPatronId(), intent.getAmount(), intent.getCurrency()));
    }

    /**
     * webhook 回调是"至少一次"投递，网关会因为我们没在超时内 200 而重试同一个事件——
     * 已经是 CAPTURED 的意图再收到一次 succeeded 事件直接跳过，不能重复发布 PaymentCaptured
     * （下游 Membership 会重复累计消费）。
     */
    private void capture(PaymentIntent intent) {
        if (intent.getStatus() == PaymentStatus.CAPTURED) {
            return;
        }
        intent.markCaptured();
        paymentIntentRepository.save(intent);
        ledgerEntryRepository.save(LedgerEntry.record(intent.getId(), null,
                LedgerDirection.CREDIT, intent.getAmount(), intent.getCurrency()));
        eventPublisher.publish(new PaymentCapturedEvent(intent.getId(), intent.getSourceType(),
                intent.getSourceId(), intent.getPatronId(), intent.getAmount(), intent.getCurrency()));
    }

    private PaymentProvider providerOf(PaymentIntent intent) {
        for (PaymentProvider provider : PaymentProvider.values()) {
            if (provider.code().equals(intent.getGatewayProvider())) {
                return provider;
            }
        }
        throw PaymentException.gatewayNotConfigured();
    }
}
