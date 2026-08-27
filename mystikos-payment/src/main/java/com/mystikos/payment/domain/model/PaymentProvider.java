package com.mystikos.payment.domain.model;

/**
 * 调用方可选的支付渠道。每个值对应一个 {@code PaymentGatewayClient.providerCode()}，
 * {@code PaymentGatewayRegistry} 按这个 code 找具体网关实现。
 */
public enum PaymentProvider {
    STRIPE("stripe"),
    ALIPAY("alipay"),
    WECHAT_PAY("wechat_pay");

    private final String code;

    PaymentProvider(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
