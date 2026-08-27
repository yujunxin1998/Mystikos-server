package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.domain.PaymentException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 支付宝境内商户号只结算人民币，不像 Stripe 那样要处理零小数位货币这类边界情况——
 * 直接拒绝非 CNY 请求，好过按错误汇率静默换算造成资损。
 */
final class AlipayAmountConverter {

    private AlipayAmountConverter() {
    }

    /** 支付宝金额字段是"元.角分"的字符串，如 88.00。 */
    static String toYuanString(BigDecimal amount, String currency) {
        requireCny(currency);
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    static void requireCny(String currency) {
        if (!"CNY".equalsIgnoreCase(currency)) {
            throw PaymentException.gatewayError("支付宝只支持 CNY 结算，收到：" + currency);
        }
    }
}
