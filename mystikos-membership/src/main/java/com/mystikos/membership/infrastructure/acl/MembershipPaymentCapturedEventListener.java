package com.mystikos.membership.infrastructure.acl;

import com.mystikos.membership.application.service.MembershipApplicationService;
import com.mystikos.payment.domain.event.PaymentCapturedEvent;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 累计消费的真正事件源：Payment 落地后不再只订阅 Gifting 的 GiftSentEvent，
 * 覆盖 Booking/Commerce/Gifting 三个来源，见 domain-model.md 的上下文映射。
 *
 * <p>故意不算 WALLET_RECHARGE——充值只是把钱挪进自己的余额，不是消费；
 * 真正花掉这笔钱时（比如送礼）会走钱包内部扣款，另发一条 sourceType=GIFT 的
 * PaymentCaptured。两条都算的话，充值 100 送礼 50 会被记成消费 150，是重复计算。
 *
 * <p>类名带 Membership 前缀——全模块扁平扫描下不能和 Booking/Commerce 各自的
 * 同职责监听器简单类名重复，否则启动时报 ConflictingBeanDefinitionException。
 */
@Component
public class MembershipPaymentCapturedEventListener {

    private final MembershipApplicationService membershipApplicationService;

    public MembershipPaymentCapturedEventListener(MembershipApplicationService membershipApplicationService) {
        this.membershipApplicationService = membershipApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PaymentCapturedEvent event) {
        if (event.getSourceType() == SourceType.WALLET_RECHARGE) {
            return;
        }
        membershipApplicationService.accrueSpend(event.getPatronId(), event.getAmount());
    }
}
