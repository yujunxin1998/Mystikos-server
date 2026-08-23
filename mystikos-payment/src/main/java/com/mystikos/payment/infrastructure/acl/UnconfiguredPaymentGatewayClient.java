package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.application.port.GatewayIntentResult;
import com.mystikos.payment.application.port.GatewayWebhookEvent;
import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.domain.PaymentException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 本地没配 STRIPE_SECRET_KEY 时的兜底实现——让应用照常启动，调用到支付相关用例时
 * 抛出明确的"支付网关未配置"错误，而不是启动失败或者拿空 key 调 Stripe API
 * 换回一个更难懂的异常。配好 secret-key 后 {@link StripeGatewayClient} 会顶替这个 Bean。
 */
@Component
@ConditionalOnExpression("'${mystikos.payment.stripe.secret-key:}'.isEmpty()")
public class UnconfiguredPaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public String providerCode() {
        return "unconfigured";
    }

    @Override
    public GatewayIntentResult createIntent(String idempotencyKey, BigDecimal amount, String currency,
                                             Map<String, String> metadata) {
        throw PaymentException.gatewayNotConfigured();
    }

    @Override
    public GatewayWebhookEvent parseWebhookEvent(String rawPayload, String signatureHeader) {
        throw PaymentException.gatewayNotConfigured();
    }

    @Override
    public void refund(String gatewayRef, BigDecimal amount) {
        throw PaymentException.gatewayNotConfigured();
    }

    @Override
    public String createConnectAccount(String email) {
        throw PaymentException.gatewayNotConfigured();
    }

    @Override
    public String createConnectOnboardingLink(String connectAccountId, String returnUrl, String refreshUrl) {
        throw PaymentException.gatewayNotConfigured();
    }

    @Override
    public boolean isPayoutReady(String connectAccountId) {
        throw PaymentException.gatewayNotConfigured();
    }

    @Override
    public String transferToConnectAccount(String connectAccountId, BigDecimal amount, String currency,
                                            String idempotencyKey) {
        throw PaymentException.gatewayNotConfigured();
    }
}
