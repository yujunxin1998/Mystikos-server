package com.mystikos.membership.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.common.level.LevelResolver;
import com.mystikos.membership.application.command.SaveMembershipTierCommand;
import com.mystikos.membership.domain.event.MembershipTierDowngradedEvent;
import com.mystikos.membership.domain.event.MembershipTierUpgradedEvent;
import com.mystikos.membership.domain.model.MembershipAccount;
import com.mystikos.membership.domain.model.MembershipTierDefinition;
import com.mystikos.membership.domain.repository.MembershipAccountRepository;
import com.mystikos.membership.domain.repository.MembershipTierDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MembershipApplicationService {

    private final MembershipAccountRepository membershipAccountRepository;
    private final MembershipTierDefinitionRepository tierDefinitionRepository;
    private final DomainEventPublisher eventPublisher;

    public MembershipApplicationService(MembershipAccountRepository membershipAccountRepository,
                                         MembershipTierDefinitionRepository tierDefinitionRepository,
                                         DomainEventPublisher eventPublisher) {
        this.membershipAccountRepository = membershipAccountRepository;
        this.tierDefinitionRepository = tierDefinitionRepository;
        this.eventPublisher = eventPublisher;
    }

    public MembershipView getMembership(Long patronId) {
        List<MembershipTierDefinition> tiers = tierDefinitionRepository.findAll();
        return membershipAccountRepository.findByPatronId(patronId)
                .map(a -> toView(a, resolveDefinition(a.getCurrentTierCode(), tiers)))
                .orElseGet(() -> {
                    MembershipTierDefinition base = LevelResolver.resolve(tiers, BigDecimal.ZERO);
                    return new MembershipView(patronId, base.getLevel(), base.getCode(),
                            base.getDisplayName(), BigDecimal.ZERO);
                });
    }

    public List<MembershipTierDefinition> listTiers() {
        return tierDefinitionRepository.findAll();
    }

    /**
     * 累加消费并按需升级。触发源：{@link com.mystikos.membership.infrastructure.acl.MembershipPaymentCapturedEventListener}
     * 订阅 Payment 的 PaymentCapturedEvent，覆盖 Booking/Commerce/Gifting 三个来源，排除 WALLET_RECHARGE。
     */
    @Transactional
    public void accrueSpend(Long patronId, BigDecimal amount) {
        List<MembershipTierDefinition> tiers = tierDefinitionRepository.findAll();
        MembershipAccount account = membershipAccountRepository.findByPatronId(patronId)
                .orElseGet(() -> MembershipAccount.initiate(patronId));
        String previousTierCode = account.getCurrentTierCode();
        boolean upgraded = account.accrueSpend(amount, tiers);
        membershipAccountRepository.save(account);
        if (upgraded) {
            MembershipTierDefinition newTier = resolveDefinition(account.getCurrentTierCode(), tiers);
            eventPublisher.publish(new MembershipTierUpgradedEvent(
                    patronId, previousTierCode, account.getCurrentTierCode(), newTier.getLevel()));
        }
    }

    /**
     * 赠礼退款场景：扣减累计消费，可能导致降级。触发源：
     * {@link com.mystikos.membership.infrastructure.acl.MembershipPaymentRefundedEventListener}
     * 订阅 Payment 的 PaymentRefundedEvent（sourceType=GIFT）。
     */
    @Transactional
    public void reverseSpend(Long patronId, BigDecimal amount) {
        if (amount.signum() <= 0) {
            return;
        }
        List<MembershipTierDefinition> tiers = tierDefinitionRepository.findAll();
        MembershipAccount account = membershipAccountRepository.findByPatronId(patronId)
                .orElseGet(() -> MembershipAccount.initiate(patronId));
        String previousTierCode = account.getCurrentTierCode();
        boolean changed = account.reverseSpend(amount, tiers);
        membershipAccountRepository.save(account);
        if (changed) {
            MembershipTierDefinition newTier = resolveDefinition(account.getCurrentTierCode(), tiers);
            eventPublisher.publish(new MembershipTierDowngradedEvent(
                    patronId, previousTierCode, account.getCurrentTierCode(), newTier.getLevel()));
        }
    }

    @Transactional
    public Long saveTier(SaveMembershipTierCommand command) {
        MembershipTierDefinition definition = new MembershipTierDefinition(command.id(), command.code(),
                command.displayName(), command.displayNameEn(), command.level(),
                command.cumulativeSpendThreshold(), command.perkDescription(), command.sortOrder());
        return tierDefinitionRepository.save(definition).getId();
    }

    private MembershipTierDefinition resolveDefinition(String code, List<MembershipTierDefinition> tiers) {
        return tiers.stream().filter(t -> t.getCode().equals(code)).findFirst()
                .orElseGet(() -> LevelResolver.resolve(tiers, BigDecimal.ZERO));
    }

    private MembershipView toView(MembershipAccount account, MembershipTierDefinition tier) {
        return new MembershipView(account.getPatronId(), tier.getLevel(), tier.getCode(),
                tier.getDisplayName(), account.getCumulativeSpend());
    }
}
