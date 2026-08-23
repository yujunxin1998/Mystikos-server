package com.mystikos.payment.infrastructure.acl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * BigDecimal（元/欧元这类"大"单位）与 Stripe 要求的最小货币单位（分）互相换算。
 *
 * <p>零位小数货币（下单金额本身就是最小单位，不用再乘 100）不能一律 ×100，
 * 我们的欧洲地区种子数据里就有冰岛克朗（ISK）——按 Stripe 官方文档维护的
 * zero-decimal-currencies 列表，用错会多收/少收 100 倍，是真实的资损风险，
 * 不是理论上的边界情况。
 */
final class StripeAmountConverter {

    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of(
            "BIF", "CLP", "DJF", "GNF", "ISK", "JPY", "KMF", "KRW", "MGA",
            "PYG", "RWF", "UGX", "VND", "VUV", "XAF", "XOF", "XPF");

    private StripeAmountConverter() {
    }

    static long toMinorUnits(BigDecimal amount, String currency) {
        String upper = currency.toUpperCase();
        BigDecimal scaled = ZERO_DECIMAL_CURRENCIES.contains(upper)
                ? amount
                : amount.multiply(BigDecimal.valueOf(100));
        return scaled.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
