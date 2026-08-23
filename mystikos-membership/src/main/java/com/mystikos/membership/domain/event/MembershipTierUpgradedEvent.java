package com.mystikos.membership.domain.event;

import com.mystikos.common.event.DomainEvent;

/**
 * 会员等级升级事件。下游消费方：mystikos-identity（把 User.membershipTierLevel/Code
 * 更新成本地只读投影，见 MembershipTierUpgradedEventListener），不直接读这张表。
 */
public class MembershipTierUpgradedEvent extends DomainEvent {

    private final Long patronId;
    private final String previousTierCode;
    private final String newTierCode;
    private final int newTierLevel;

    public MembershipTierUpgradedEvent(Long patronId, String previousTierCode, String newTierCode, int newTierLevel) {
        this.patronId = patronId;
        this.previousTierCode = previousTierCode;
        this.newTierCode = newTierCode;
        this.newTierLevel = newTierLevel;
    }

    public Long getPatronId() {
        return patronId;
    }

    public String getPreviousTierCode() {
        return previousTierCode;
    }

    public String getNewTierCode() {
        return newTierCode;
    }

    public int getNewTierLevel() {
        return newTierLevel;
    }
}
