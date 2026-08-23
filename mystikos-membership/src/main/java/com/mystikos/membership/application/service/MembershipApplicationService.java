package com.mystikos.membership.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.membership.domain.event.MembershipTierUpgradedEvent;
import com.mystikos.membership.domain.model.MembershipAccount;
import com.mystikos.membership.domain.repository.MembershipAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MembershipApplicationService {

    private final MembershipAccountRepository membershipAccountRepository;
    private final DomainEventPublisher eventPublisher;

    public MembershipApplicationService(MembershipAccountRepository membershipAccountRepository,
                                         DomainEventPublisher eventPublisher) {
        this.membershipAccountRepository = membershipAccountRepository;
        this.eventPublisher = eventPublisher;
    }

    public MembershipView getMembership(Long patronId) {
        return membershipAccountRepository.findByPatronId(patronId)
                .map(a -> new MembershipView(a.getPatronId(), a.getCurrentTier().getLevel(),
                        a.getCurrentTier().getCode(), a.getCurrentTier().getDisplayName(), a.getCumulativeSpend()))
                .orElseGet(() -> MembershipView.initial(patronId));
    }

    /**
     * 累加消费并按需升级。目前唯一的调用方是 GiftSentEventListener（临时顶替 PaymentCaptured，
     * 见 pom.xml 里的依赖说明），Payment 上下文落地后应该改为订阅 PaymentCaptured。
     */
    @Transactional
    public void accrueSpend(Long patronId, BigDecimal amount) {
        MembershipAccount account = membershipAccountRepository.findByPatronId(patronId)
                .orElseGet(() -> MembershipAccount.initiate(patronId));
        String previousTierCode = account.getCurrentTier().getCode();
        boolean upgraded = account.accrueSpend(amount);
        membershipAccountRepository.save(account);
        if (upgraded) {
            eventPublisher.publish(new MembershipTierUpgradedEvent(
                    patronId, previousTierCode, account.getCurrentTier().getCode(), account.getCurrentTier().getLevel()));
        }
    }
}
