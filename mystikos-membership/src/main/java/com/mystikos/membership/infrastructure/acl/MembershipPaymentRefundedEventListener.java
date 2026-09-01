package com.mystikos.membership.infrastructure.acl;

import com.mystikos.membership.application.service.MembershipApplicationService;
import com.mystikos.payment.domain.event.PaymentRefundedEvent;
import com.mystikos.payment.domain.model.SourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 赠礼退款时扣减累计消费，可能导致降级——秘典明确要求"因订单纠纷发生退款的礼物，
 * 对应的亲密度点数与 VIP 累计消费需同步扣除"。只处理 sourceType=GIFT：Booking/Commerce
 * 目前还没有真正走到退款用例，等它们接上退款后，这里的判断自然覆盖到（不需要改这个类，
 * PaymentRefundedEvent 对 sourceType 是泛化的）。
 *
 * <p>直接订阅 Payment 的事件而不是 Gifting 的——Membership 本来就已经依赖 Payment
 * （accrueSpend 那条边），不需要为退款单独新开一条对 Gifting 的依赖。
 */
@Component
public class MembershipPaymentRefundedEventListener {

    private final MembershipApplicationService membershipApplicationService;

    public MembershipPaymentRefundedEventListener(MembershipApplicationService membershipApplicationService) {
        this.membershipApplicationService = membershipApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PaymentRefundedEvent event) {
        if (event.getSourceType() != SourceType.GIFT) {
            return;
        }
        membershipApplicationService.reverseSpend(event.getPatronId(), event.getAmount());
    }
}
