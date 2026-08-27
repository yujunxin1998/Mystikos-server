package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.application.port.GatewayEventType;
import com.mystikos.payment.application.port.GatewayIntentResult;
import com.mystikos.payment.application.port.GatewayWebhookEvent;
import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.application.port.PayoutGatewayClient;
import com.mystikos.payment.application.port.WebhookNotification;
import com.mystikos.payment.domain.PaymentException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.TransferCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stripe 实现。后端全程不接触原始卡号——前端用 Stripe.js/Payment Element 做
 * tokenization，我们只处理 PaymentIntent id / client secret，PCI 合规范围
 * 因此被压到最小（SAQ A）。
 *
 * <p>只在配置了 secret-key 时注册这个 Bean，照
 * {@code com.mystikos.identity.infrastructure.acl.DiscordOAuthClient} 的写法——
 * 本地没配 Stripe key 时不注册，{@link com.mystikos.payment.application.service.PaymentGatewayRegistry}
 * 找不到 "stripe" 这个 providerCode 就会抛"网关未配置"，而不是启动失败或拿空 key 去调
 * Stripe API 换一个更难懂的错误。同时是目前唯一实现 {@link PayoutGatewayClient} 的网关——
 * 陪玩提现打款继续走 Stripe Connect，本地没配 secret-key 时提现相关调用见
 * {@link UnconfiguredPayoutGatewayClient}。
 */
@Component
@ConditionalOnExpression("!'${mystikos.payment.stripe.secret-key:}'.isEmpty()")
public class StripeGatewayClient implements PaymentGatewayClient, PayoutGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(StripeGatewayClient.class);

    private final String secretKey;
    private final String webhookSecret;
    private final List<String> paymentMethodTypes;

    public StripeGatewayClient(@Value("${mystikos.payment.stripe.secret-key}") String secretKey,
                                @Value("${mystikos.payment.stripe.webhook-secret}") String webhookSecret,
                                @Value("#{'${mystikos.payment.stripe.payment-method-types:card}'.split(',')}")
                                List<String> paymentMethodTypes) {
        this.secretKey = secretKey;
        this.webhookSecret = webhookSecret;
        this.paymentMethodTypes = paymentMethodTypes;
    }

    /**
     * 全局设一次 API key，之后的调用（除了需要幂等键的 create 类调用）都不用单独传
     * RequestOptions——这是 Stripe Java SDK 的常规用法，不是偷懒漏传。
     */
    @PostConstruct
    void initGlobalApiKey() {
        com.stripe.Stripe.apiKey = secretKey;
    }

    @Override
    public String providerCode() {
        return "stripe";
    }

    /** scene 对 Stripe 没有意义（不区分场景，一律走 Payment Element/Stripe.js），忽略即可。 */
    @Override
    public GatewayIntentResult createIntent(String idempotencyKey, BigDecimal amount, String currency,
                                             Map<String, String> metadata, PaymentScene scene) {
        long minorUnits = StripeAmountConverter.toMinorUnits(amount, currency);
        PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(minorUnits)
                .setCurrency(currency.toLowerCase())
                .putAllMetadata(metadata);
        paymentMethodTypes.forEach(builder::addPaymentMethodType);

        try {
            PaymentIntent intent = PaymentIntent.create(builder.build(), requestOptions(idempotencyKey));
            return GatewayIntentResult.clientSecret(intent.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            log.warn("Stripe 建单失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    @Override
    public GatewayWebhookEvent parseWebhookEvent(WebhookNotification notification) {
        String signatureHeader = notification.headers().get("Stripe-Signature");
        Event event;
        try {
            event = Webhook.constructEvent(notification.rawBody(), signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook 验签失败：{}", e.getMessage());
            throw PaymentException.webhookSignatureInvalid();
        }

        return switch (event.getType()) {
            case "payment_intent.succeeded" -> new GatewayWebhookEvent(
                    GatewayEventType.CAPTURED, extractPaymentIntentId(event), null);
            case "payment_intent.payment_failed" -> new GatewayWebhookEvent(
                    GatewayEventType.FAILED, extractPaymentIntentId(event), extractFailureReason(event));
            case "charge.refunded" -> new GatewayWebhookEvent(
                    GatewayEventType.REFUNDED, extractPaymentIntentIdFromCharge(event), null);
            default -> new GatewayWebhookEvent(GatewayEventType.IGNORED, null, null);
        };
    }

    @Override
    public void refund(String gatewayRef, BigDecimal amount) {
        try {
            Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(gatewayRef)
                    .build());
        } catch (StripeException e) {
            log.warn("Stripe 退款失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    @Override
    public String createConnectAccount(String email) {
        try {
            Account account = Account.create(AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setEmail(email)
                    .setCapabilities(AccountCreateParams.Capabilities.builder()
                            .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                    .setRequested(true).build())
                            .build())
                    .build());
            return account.getId();
        } catch (StripeException e) {
            log.warn("Stripe Connect 建账户失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    @Override
    public String createConnectOnboardingLink(String connectAccountId, String returnUrl, String refreshUrl) {
        try {
            AccountLink link = AccountLink.create(AccountLinkCreateParams.builder()
                    .setAccount(connectAccountId)
                    .setReturnUrl(returnUrl)
                    .setRefreshUrl(refreshUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build());
            return link.getUrl();
        } catch (StripeException e) {
            log.warn("Stripe Connect onboarding link 生成失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    @Override
    public boolean isPayoutReady(String connectAccountId) {
        try {
            Account account = Account.retrieve(connectAccountId, requestOptions(null));
            return Boolean.TRUE.equals(account.getPayoutsEnabled());
        } catch (StripeException e) {
            log.warn("Stripe Connect 账户状态查询失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    @Override
    public String transferToConnectAccount(String connectAccountId, BigDecimal amount, String currency,
                                            String idempotencyKey) {
        long minorUnits = StripeAmountConverter.toMinorUnits(amount, currency);
        try {
            Transfer transfer = Transfer.create(TransferCreateParams.builder()
                    .setAmount(minorUnits)
                    .setCurrency(currency.toLowerCase())
                    .setDestination(connectAccountId)
                    .build(), requestOptions(idempotencyKey));
            return transfer.getId();
        } catch (StripeException e) {
            log.warn("Stripe Connect 打款失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    private RequestOptions requestOptions(String idempotencyKey) {
        RequestOptions.RequestOptionsBuilder builder = RequestOptions.builder().setApiKey(secretKey);
        if (idempotencyKey != null) {
            builder.setIdempotencyKey(idempotencyKey);
        }
        return builder.build();
    }

    private String extractPaymentIntentId(Event event) {
        return deserialize(event)
                .filter(PaymentIntent.class::isInstance)
                .map(obj -> ((PaymentIntent) obj).getId())
                .orElse(null);
    }

    private String extractFailureReason(Event event) {
        return deserialize(event)
                .filter(PaymentIntent.class::isInstance)
                .map(obj -> (PaymentIntent) obj)
                .map(intent -> intent.getLastPaymentError() != null ? intent.getLastPaymentError().getMessage() : null)
                .orElse(null);
    }

    private String extractPaymentIntentIdFromCharge(Event event) {
        return deserialize(event)
                .filter(Charge.class::isInstance)
                .map(obj -> ((Charge) obj).getPaymentIntent())
                .orElse(null);
    }

    private Optional<StripeObject> deserialize(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        return deserializer.getObject();
    }
}
