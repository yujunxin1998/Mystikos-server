package com.mystikos.relationship.application.service;

import com.mystikos.relationship.domain.model.IntimacyRecord;

import java.math.BigDecimal;

public record IntimacyView(Long patronId, Long companionId, String levelCode, BigDecimal progressValue) {

    public static IntimacyView empty(Long patronId, Long companionId) {
        return new IntimacyView(patronId, companionId, IntimacyRecord.INITIAL_LEVEL_CODE, BigDecimal.ZERO);
    }
}
