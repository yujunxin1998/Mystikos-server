package com.mystikos.membership.application.service;

import com.mystikos.membership.domain.model.DefaultMembershipTier;

import java.math.BigDecimal;

public record MembershipView(Long patronId, int tierLevel, String tierCode, String tierDisplayName,
                              BigDecimal cumulativeSpend) {

    public static MembershipView initial(Long patronId) {
        DefaultMembershipTier lv1 = DefaultMembershipTier.LV1;
        return new MembershipView(patronId, lv1.getLevel(), lv1.getCode(), lv1.getDisplayName(), BigDecimal.ZERO);
    }
}
