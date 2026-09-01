package com.mystikos.relationship.domain.event;

import com.mystikos.common.event.DomainEvent;

/**
 * 亲密度等级变化事件（升级或退款导致的降级）。code 是字符串（十级阶梯的等级编码），
 * 不是裸整数序号——见 {@link com.mystikos.relationship.domain.model.IntimacyRecord} 的说明。
 * 目前没有任何订阅方，是 Notification 模块"送礼后弹升级动画"这类需求现成的接入点。
 */
public class IntimacyLevelChangedEvent extends DomainEvent {

    private final Long patronId;
    private final Long companionId;
    private final String previousLevelCode;
    private final String newLevelCode;

    public IntimacyLevelChangedEvent(Long patronId, Long companionId, String previousLevelCode, String newLevelCode) {
        this.patronId = patronId;
        this.companionId = companionId;
        this.previousLevelCode = previousLevelCode;
        this.newLevelCode = newLevelCode;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public String getPreviousLevelCode() {
        return previousLevelCode;
    }

    public String getNewLevelCode() {
        return newLevelCode;
    }
}
