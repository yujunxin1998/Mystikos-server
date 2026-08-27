package com.mystikos.payment.application.port;

import java.math.BigDecimal;

/**
 * 陪玩提现打款能力，目前只有 Stripe Connect Express 实现这个接口——支付宝/微信没有
 * 对等的打款账户体系，这次接入不做（陪玩提现继续走 Stripe Connect）。
 * 单独拆出来是为了 {@link PaymentGatewayClient} 能只描述收款下单，不用被迫塞打款相关方法。
 */
public interface PayoutGatewayClient {

    /** 创建一个 Stripe Connect Express 账户，返回网关侧账户 id。 */
    String createConnectAccount(String email);

    /** 生成 Connect 账户完成入驻资料所需的一次性跳转链接。 */
    String createConnectOnboardingLink(String connectAccountId, String returnUrl, String refreshUrl);

    /** 实时查询这个 Connect 账户是否已经通过 Stripe 审核、可以接收打款——不缓存，每次都问网关。 */
    boolean isPayoutReady(String connectAccountId);

    /** 从平台余额打款给某个已完成入驻的 Connect 账户，返回网关侧转账 id。 */
    String transferToConnectAccount(String connectAccountId, BigDecimal amount, String currency, String idempotencyKey);
}
