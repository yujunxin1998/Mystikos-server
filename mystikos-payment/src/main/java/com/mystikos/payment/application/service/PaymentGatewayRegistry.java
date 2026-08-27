package com.mystikos.payment.application.service;

import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.model.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按 providerCode 路由到具体网关 Bean。每个 {@link PaymentGatewayClient} 实现自己按配置是否
 * 齐全决定要不要注册（照 {@code StripeGatewayClient} 的 {@code @ConditionalOnExpression} 写法），
 * 这里只是收集当前实际注册上的那些，找不到对应 provider 时统一抛"网关未配置"，
 * 取代原来"Stripe/兜底二选一"的单 Bean 设计——现在 0～3 家网关同时注册都是合法状态。
 */
@Component
public class PaymentGatewayRegistry {

    private final Map<String, PaymentGatewayClient> clientsByProviderCode;

    public PaymentGatewayRegistry(List<PaymentGatewayClient> clients) {
        this.clientsByProviderCode = clients.stream()
                .collect(Collectors.toMap(PaymentGatewayClient::providerCode, Function.identity()));
    }

    public PaymentGatewayClient get(PaymentProvider provider) {
        PaymentGatewayClient client = clientsByProviderCode.get(provider.code());
        if (client == null) {
            throw PaymentException.gatewayNotConfigured();
        }
        return client;
    }
}
