package com.mystikos.membership.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.membership.domain.event.MembershipTierDowngradedEvent;
import com.mystikos.membership.domain.event.MembershipTierUpgradedEvent;
import com.mystikos.membership.domain.model.MembershipAccount;
import com.mystikos.membership.domain.model.MembershipTierDefinition;
import com.mystikos.membership.domain.repository.MembershipAccountRepository;
import com.mystikos.membership.domain.repository.MembershipTierDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 覆盖秘典"退款要同步扣减 VIP 累计消费，可能导致降级"的要求——这是对现有
 * "等级只增不减"假设的一次刻意松动，只在退款路径生效。
 */
class MembershipApplicationServiceTest {

    private MembershipAccountRepository accountRepository;
    private DomainEventPublisher eventPublisher;
    private MembershipApplicationService service;

    @BeforeEach
    void setUp() {
        accountRepository = mock(MembershipAccountRepository.class);
        MembershipTierDefinitionRepository tierDefinitionRepository = mock(MembershipTierDefinitionRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        service = new MembershipApplicationService(accountRepository, tierDefinitionRepository, eventPublisher);

        when(tierDefinitionRepository.findAll()).thenReturn(List.of(
                new MembershipTierDefinition(1L, "VISITOR", "访客", "Visitor", 0, BigDecimal.ZERO, null, 1),
                new MembershipTierDefinition(2L, "WANDERER", "旅人", "Wanderer", 1, BigDecimal.valueOf(98), null, 2)));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, MembershipAccount.class));
    }

    @Test
    void accrueSpendPublishesUpgradeEvent() {
        when(accountRepository.findByPatronId(1L)).thenReturn(Optional.empty());

        service.accrueSpend(1L, BigDecimal.valueOf(100));

        org.mockito.ArgumentCaptor<MembershipTierUpgradedEvent> captor =
                org.mockito.ArgumentCaptor.forClass(MembershipTierUpgradedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getNewTierCode()).isEqualTo("WANDERER");
    }

    @Test
    void reverseSpendPublishesDowngradeEventWhenTierDrops() {
        MembershipAccount account = MembershipAccount.initiate(1L);
        account.accrueSpend(BigDecimal.valueOf(100), List.of(
                new MembershipTierDefinition(1L, "VISITOR", "访客", "Visitor", 0, BigDecimal.ZERO, null, 1),
                new MembershipTierDefinition(2L, "WANDERER", "旅人", "Wanderer", 1, BigDecimal.valueOf(98), null, 2)));
        when(accountRepository.findByPatronId(1L)).thenReturn(Optional.of(account));

        service.reverseSpend(1L, BigDecimal.valueOf(100));

        org.mockito.ArgumentCaptor<MembershipTierDowngradedEvent> captor =
                org.mockito.ArgumentCaptor.forClass(MembershipTierDowngradedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getNewTierCode()).isEqualTo("VISITOR");
    }

    @Test
    void reverseSpendWithNonPositiveAmountIsNoop() {
        service.reverseSpend(1L, BigDecimal.ZERO);

        verify(accountRepository, never()).findByPatronId(any());
        verify(eventPublisher, never()).publish(any());
    }
}
