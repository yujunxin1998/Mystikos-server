package com.mystikos.relationship.domain.event;

import com.mystikos.common.event.DomainEvent;

public class IntimacyStageChangedEvent extends DomainEvent {

    private final Long patronId;
    private final Long companionId;
    private final int previousStage;
    private final int newStage;

    public IntimacyStageChangedEvent(Long patronId, Long companionId, int previousStage, int newStage) {
        this.patronId = patronId;
        this.companionId = companionId;
        this.previousStage = previousStage;
        this.newStage = newStage;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public int getPreviousStage() {
        return previousStage;
    }

    public int getNewStage() {
        return newStage;
    }
}
