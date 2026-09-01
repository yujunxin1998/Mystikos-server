package com.mystikos.membership.domain.event;

import com.mystikos.common.event.DomainEvent;

/**
 * 会员等级降级事件——目前只有一条触发路径：赠礼退款导致累计消费回退。和
 * {@link MembershipTierUpgradedEvent} 分开是因为下游反应应该不同（升级可以弹庆祝动画，
 * 降级不应该弹一样的东西），但都会被 mystikos-identity 同步进 User 的本地投影字段。
 */
public class MembershipTierDowngradedEvent extends DomainEvent {

    private final Long patronId;
    private final String previousTierCode;
    private final String newTierCode;
    private final int newTierLevel;

    public MembershipTierDowngradedEvent(Long patronId, String previousTierCode, String newTierCode, int newTierLevel) {
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
