package com.mystikos.payment.domain.model;

/**
 * 陪玩收款账户和 Stripe Connect 账户的映射。属于 Payment 自己的支付基础设施数据，
 * 不下沉业务细节（不知道也不关心陪玩资料，只知道"这个 userId 对应哪个 Connect 账户"）。
 *
 * <p>是否真正能收款（入驻资料是否已被 Stripe 审核通过）不在这张表里存一个可能过期的
 * 布尔标记——本轮没接 Stripe Connect 的 account.updated webhook，存了也没人更新它，
 * 那是比没有这个字段更危险的假象。审批提现时改为实时调
 * {@link com.mystikos.payment.application.port.PaymentGatewayClient#isPayoutReady} 问 Stripe。
 */
public final class CompanionPayoutAccount {

    private Long id;
    private final Long userId;
    private final String stripeConnectAccountId;

    private CompanionPayoutAccount(Long id, Long userId, String stripeConnectAccountId) {
        this.id = id;
        this.userId = userId;
        this.stripeConnectAccountId = stripeConnectAccountId;
    }

    public static CompanionPayoutAccount create(Long userId, String stripeConnectAccountId) {
        return new CompanionPayoutAccount(null, userId, stripeConnectAccountId);
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static CompanionPayoutAccount restore(Long id, Long userId, String stripeConnectAccountId) {
        return new CompanionPayoutAccount(id, userId, stripeConnectAccountId);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStripeConnectAccountId() {
        return stripeConnectAccountId;
    }
}
