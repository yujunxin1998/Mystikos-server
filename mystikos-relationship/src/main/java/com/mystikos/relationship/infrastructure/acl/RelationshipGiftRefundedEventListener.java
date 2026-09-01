package com.mystikos.relationship.infrastructure.acl;

import com.mystikos.gifting.domain.event.GiftRefundedEvent;
import com.mystikos.relationship.application.service.RelationshipApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订阅 Gifting 的退款事件，扣减对应的亲密度进度值——复用 Relationship 已经依赖 Gifting
 * 的这条边（订阅 GiftSentEvent 那条），不需要新开一条模块依赖。
 */
@Component
public class RelationshipGiftRefundedEventListener {

    private final RelationshipApplicationService relationshipApplicationService;

    public RelationshipGiftRefundedEventListener(RelationshipApplicationService relationshipApplicationService) {
        this.relationshipApplicationService = relationshipApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(GiftRefundedEvent event) {
        relationshipApplicationService.reverseProgress(
                event.getPatronId(), event.getCompanionId(), event.getIntimacyValue());
    }
}
