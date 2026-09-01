package com.mystikos.relationship.infrastructure.acl;

import com.mystikos.gifting.domain.event.GiftSentEvent;
import com.mystikos.relationship.application.service.RelationshipApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订阅 Gifting 的赠礼事件，累加亲密度进度——用 {@code intimacyValue}（= 原价 x 档位倍率），
 * 不是原价 {@code amount}，秘典的规则是"档位倍率只影响亲密度"。Booking 完成度暂时接不上
 * （Booking 还没有任何用例真正推进到 COMPLETED 状态并发事件），先只接 Gifting 这一路。
 *
 * 用 AFTER_COMMIT 而不是普通 @EventListener：Gifting 那笔赠礼流水的写入必须先真正提交，
 * 亲密度这边的累加才跟着做，避免赠礼事务后续回滚但亲密度已经加过的不一致；
 * AFTER_COMMIT 回调在原事务之外单独执行，失败也不会影响赠礼本身已经成功的结果。
 */
@Component
public class RelationshipGiftSentEventListener {

    private final RelationshipApplicationService relationshipApplicationService;

    public RelationshipGiftSentEventListener(RelationshipApplicationService relationshipApplicationService) {
        this.relationshipApplicationService = relationshipApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(GiftSentEvent event) {
        relationshipApplicationService.accrueProgress(
                event.getPatronId(), event.getCompanionId(), event.getIntimacyValue());
    }
}
