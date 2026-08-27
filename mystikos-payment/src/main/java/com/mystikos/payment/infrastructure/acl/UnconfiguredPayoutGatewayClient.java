package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.application.port.PayoutGatewayClient;
import com.mystikos.payment.domain.PaymentException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 本地没配 STRIPE_SECRET_KEY 时的兜底实现——{@code WalletApplicationService} 走构造函数
 * 硬依赖 {@link PayoutGatewayClient}（陪玩提现只有 Stripe Connect 一家，不像收款下单那样
 * 走 {@code PaymentGatewayRegistry} 那套"零到多个网关都合法"的路由），没有这个兜底 Bean
 * 的话 Stripe 没配置时应用直接起不来。调用到提现相关用例时抛出明确的"支付网关未配置"，
 * 而不是启动失败或者拿空 key 调 Stripe API 换回一个更难懂的异常。
 */
@Component
@ConditionalOnExpression("'${mystikos.payment.stripe.secret-key:}'.isEmpty()")
public class UnconfiguredPayoutGatewayClient implements PayoutGatewayClient {

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
