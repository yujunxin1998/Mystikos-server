package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.domain.PaymentException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 微信支付商户号只结算人民币，金额字段是整数分——理由同 {@link AlipayAmountConverter}。
 */
final class WechatAmountConverter {

    private WechatAmountConverter() {
    }

    static int toFen(BigDecimal amount, String currency) {
        if (!"CNY".equalsIgnoreCase(currency)) {
            throw PaymentException.gatewayError("微信支付只支持 CNY 结算，收到：" + currency);
        }
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
