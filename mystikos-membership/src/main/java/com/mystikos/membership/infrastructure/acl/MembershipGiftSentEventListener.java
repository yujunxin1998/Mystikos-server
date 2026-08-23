package com.mystikos.membership.infrastructure.acl;

import com.mystikos.gifting.domain.event.GiftSentEvent;
import com.mystikos.membership.application.service.MembershipApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 临时顶替方案：按 domain-model.md 的事件图，Membership 的消费累计应该订阅
 * mystikos-payment 的 PaymentCaptured，但 Payment 上下文还没建（S6，这次不在范围内）。
 * 先用 Gifting 的赠礼事件当"确认消费"信号顶替，让会员成长在赠礼场景下能真实跑通。
 *
 * TODO(payment-integration): mystikos-payment 落地、真正发出 PaymentCaptured 事件后，
 * 把这个监听器删掉，改成订阅 PaymentCaptured（覆盖赠礼/预约/商城三个来源，而不是只有赠礼）。
 */
@Component
public class MembershipGiftSentEventListener {

    private final MembershipApplicationService membershipApplicationService;

    public MembershipGiftSentEventListener(MembershipApplicationService membershipApplicationService) {
        this.membershipApplicationService = membershipApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(GiftSentEvent event) {
        membershipApplicationService.accrueSpend(event.getPatronId(), event.getAmount());
    }
}
