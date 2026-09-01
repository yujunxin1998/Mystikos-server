package com.mystikos.membership.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipAccountTest {

    private final List<MembershipTierDefinition> tiers = List.of(
            new MembershipTierDefinition(1L, "VISITOR", "访客", "Visitor", 0, BigDecimal.ZERO, null, 1),
            new MembershipTierDefinition(2L, "WANDERER", "旅人", "Wanderer", 1, BigDecimal.valueOf(98), null, 2),
            new MembershipTierDefinition(3L, "SEEKER", "探索者", "Seeker", 2, BigDecimal.valueOf(328), null, 3));

    @Test
    void accrueSpendUpgradesToHighestSatisfiedTier() {
        MembershipAccount account = MembershipAccount.initiate(1L);

        boolean upgraded = account.accrueSpend(BigDecimal.valueOf(400), tiers);

        assertThat(upgraded).isTrue();
        assertThat(account.getCurrentTierCode()).isEqualTo("SEEKER");
        assertThat(account.getCumulativeSpend()).isEqualByComparingTo("400");
    }

    @Test
    void accrueSpendWithinSameTierDoesNotFireUpgrade() {
        MembershipAccount account = MembershipAccount.initiate(1L);
        account.accrueSpend(BigDecimal.valueOf(100), tiers);

        boolean upgraded = account.accrueSpend(BigDecimal.valueOf(50), tiers);

        assertThat(upgraded).isFalse();
        assertThat(account.getCurrentTierCode()).isEqualTo("WANDERER");
    }

    @Test
    void reverseSpendFloorsAtZeroAndAllowsDowngrade() {
        MembershipAccount account = MembershipAccount.initiate(1L);
        account.accrueSpend(BigDecimal.valueOf(400), tiers);
        assertThat(account.getCurrentTierCode()).isEqualTo("SEEKER");

        boolean changed = account.reverseSpend(BigDecimal.valueOf(1000), tiers);

        assertThat(changed).isTrue();
        assertThat(account.getCumulativeSpend()).isEqualByComparingTo("0");
        assertThat(account.getCurrentTierCode()).isEqualTo("VISITOR");
    }
}
