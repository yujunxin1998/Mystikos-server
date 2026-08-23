package com.mystikos.leaderboard.infrastructure.acl;

import com.mystikos.gifting.domain.event.GiftSentEvent;
import com.mystikos.leaderboard.application.service.LeaderboardApplicationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订阅 Gifting 的赠礼事件，同时累加陪玩魅力值（收礼方）和老板守护值（送礼方）。
 * BookingCompleted 那一路同样接不上，见 mystikos-relationship 的 GiftSentEventListener 说明。
 */
@Component
public class LeaderboardGiftSentEventListener {

    private final LeaderboardApplicationService leaderboardApplicationService;

    public LeaderboardGiftSentEventListener(LeaderboardApplicationService leaderboardApplicationService) {
        this.leaderboardApplicationService = leaderboardApplicationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(GiftSentEvent event) {
        leaderboardApplicationService.accrueCharm(event.getCompanionId(), event.getAmount());
        leaderboardApplicationService.accrueGuard(event.getPatronId(), event.getAmount());
    }
}
