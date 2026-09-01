package com.mystikos.membership.application.command;

import java.math.BigDecimal;

public record SaveMembershipTierCommand(
        Long id,
        String code,
        String displayName,
        String displayNameEn,
        int level,
        BigDecimal cumulativeSpendThreshold,
        String perkDescription,
        int sortOrder
) {
}
